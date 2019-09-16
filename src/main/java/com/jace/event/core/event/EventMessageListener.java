package com.jace.event.core.event;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.jace.event.core.common.serviceconfig.ServiceConfig;
import com.jace.event.core.common.util.ContextHolderEx;
import com.jace.event.core.event.annocationhandler.EventAnnotationBeanPostProcessor;
import com.jace.event.core.event.annocationhandler.EventInvokeHandler;
import com.jace.event.core.event.httpevent.HttpEventListener;
import com.jace.event.core.message.MessageAck;
import com.jace.event.core.message.MessageListener;
import com.jace.event.core.message.dto.Message;
import com.jace.event.support.event.Event;
import com.jace.event.support.event.ReturnEvents;
import com.jace.event.support.log.IgnoreLog;
import com.jace.event.support.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventMessageListener implements MessageListener {
	
	@Autowired(required = false)
	private HttpEventListener httpEventListener = null;
	
	@Autowired
	private EventHolderEx eventHolder = null;
	
	@Resource(name="stringRedisTemplate")
	private ValueOperations<String, String> valOps = null;
		
	private boolean checkScenario = false;
	
	@Autowired
	private EventConfig eventConfig = null;
	
	@Autowired
	private ServiceConfig serviceConfig = null;
	
	@PostConstruct
	private void init() {
		checkScenario = ("true".equalsIgnoreCase(serviceConfig.getConfig("check.scenario")));
	}
	
	protected Object invokeEvent(EventInvokeHandler eventInvokeHandler, Event event, Object[] params, Message message) {
		long begin = System.currentTimeMillis();
		String eventKey = EventAnnotationBeanPostProcessor.getEventKey(event);
		IgnoreLog ignoreLog = eventInvokeHandler.getIgnoreLog();
		if (ignoreLog == null || !ignoreLog.ignoreAll()) {
			if (ignoreLog != null && ignoreLog.ignoreInputParam()) {
				log.info("##### Begin Event[{}] Param[{}]", eventKey, "*");	
			} else {
				log.info("##### Begin Event[{}] Param[{}]", eventKey, params);
			}
			log.info("##### Event[{}] CallInfo[msgId={}, user={}, caller={}, callerId={}, sourceId={}, scenario={}]", 
					message.getTopic(), message.getMsgId(), message.getUserId(), message.getCaller(), message.getCallRequestId(), 
					message.getSourceId(), message.getScenario());
		}
		Object result = eventInvokeHandler.invoke(params);
		long end = System.currentTimeMillis();
		if (ignoreLog == null || !ignoreLog.ignoreAll()) {
			if (ignoreLog != null && ignoreLog.ignoreOutputParam()) {
				log.info("##### Finish Event[{}] Cost[{}ms] Result[{}]", eventKey, end - begin, "*");
			} else {
				log.info("##### Finish Event[{}] Cost[{}ms] Result[{}]", eventKey, end - begin, result);
			}
			
		}
		return result;
	}
	
	protected void handleTopic(Message message, String topic, EventInvokeHandler eventInvokeHandler, Event event, Object httpCache) {
		Object param = getMessageData(message, eventInvokeHandler.getMethod().getGenericParameterTypes()[0]);
		invokeMethodAndPublishEvent(event, message, eventInvokeHandler, new Object[]{param}, httpCache);
	}
	
	protected void handleTopicsOr(Message message, String topic, EventInvokeHandler eventInvokeHandler, Event event, Object httpCache) {
		String[] topics = event.listenTopicsOr();
		String[] topicParamAnns = EventAnnotationBeanPostProcessor.getTopicParams(event);
		Object[] params = new Object[topics.length];
		int index = 0;
		for (String topicParamAnn : topicParamAnns) {
			if (topicParamAnn.equals(message.getTopic())) {
				params[index] = getMessageData(message, eventInvokeHandler.getMethod().getGenericParameterTypes()[index]);
			} else {
				params[index] = null;
			}
			index++;
		}
		invokeMethodAndPublishEvent(event, message, eventInvokeHandler, params, httpCache);
	}
	
	protected void handleTopicsAnd(Message message, String topic, EventInvokeHandler eventInvokeHandler, Event event, Object httpCache) {
		String[] topics = event.listenTopicsAnd();
		String sourceId = message.getSourceId();
		valOps.set(sourceId + "/" + topic, message.getDataJsonString(), event.cacheSecond(), TimeUnit.SECONDS);
		
		boolean allTopicOK = true;
		Map<String, String> topicParams = new HashMap<String, String>();
		for (String t : topics) {
			if (t.equals(topic)) {
				topicParams.put(t, message.getDataJsonString());
			} else {
				String redisKey = sourceId + "/" + t;
				String redisValue = valOps.get(redisKey);
				if (redisValue == null) {
					if (eventInvokeHandler.getIgnoreLog() == null || !eventInvokeHandler.getIgnoreLog().ignoreAll()) {
						log.info("##### Event[{}] missing message with topic[{}], event will not be invoked now.", EventAnnotationBeanPostProcessor.getEventKey(event), t);
						log.info("##### Event[{}] CallInfo[msgId={}, user={}, caller={}, callerId={}, sourceId={}, scenario={}]", 
								message.getTopic(), message.getMsgId(), message.getUserId(), message.getCaller(), message.getCallRequestId(), 
								message.getSourceId(), message.getScenario());
					}
					allTopicOK = false;
					break;
				} else {
					topicParams.put(t, redisValue);
				}
			}
		}
		if (allTopicOK) {
			String lockKey = sourceId + "/" + "LOCK";
			boolean lock = valOps.setIfAbsent(lockKey, "LOCK");
			if (lock) {
				valOps.getOperations().expire(lockKey, event.cacheSecond(), TimeUnit.SECONDS);
				String[] topicParamAnns = EventAnnotationBeanPostProcessor.getTopicParams(event);
				Object[] params = new Object[topics.length];
				int index = 0;
				Type[] types = eventInvokeHandler.getMethod().getGenericParameterTypes();
				for (String topicParamAnn : topicParamAnns) {
					params[index] = getMessageData(topicParams.get(topicParamAnn), types[index]);
					index++;
				}
				for (String t : topics) {
					String redisKey = sourceId + "/" + t;
					valOps.getOperations().delete(redisKey);
				}
				invokeMethodAndPublishEvent(event, message, eventInvokeHandler, params, httpCache);
			}
		}
	}
	
	protected void postMessage(Message message) {
		
		String topic = message.getTopic();
		EventInvokeHandler eventInvokeHandler = EventAnnotationBeanPostProcessor.getEventInvokeHandler(topic);
		Event event = eventInvokeHandler.getEvent();
		Object httpCache = null;
		if (event.isHttpEvent()) {
			httpCache = httpEventListener.canInvoke(message);
			if (httpCache == null) {
				return;
			}
		}
		if (event.listenTopic().length() > 0) {
			handleTopic(message, topic, eventInvokeHandler, event, httpCache);
		} else if (event.listenTopicsAnd().length > 0){
			handleTopicsAnd(message, topic, eventInvokeHandler, event, httpCache);
		} else {
			handleTopicsOr(message, topic, eventInvokeHandler, event, httpCache);
		}
	}

	protected void publishSuccessEvent(Message message, EventInvokeHandler handler, Object returnObject) {
		if (handler.getMethod().getReturnType() == null) {
			return;
		}
		if (returnObject instanceof ReturnEvents) {
			ReturnEvents returnEvents = (ReturnEvents)returnObject;
			String[] topics = handler.getEvent().returnTopics();
			for (String t : topics) {
				Object re = returnEvents.getReturnObject(t);
				if (re != null) {
					eventHolder.publishEvent(t, re);
				}
			}
		} else {
			if (returnObject != null) {
				eventHolder.publishEvent(handler.getEvent().returnTopic(), returnObject);
			}
		}
	}
	
	protected void publishErrorEvent(Message message, EventInvokeHandler handler, String exceptionString) {
		if (handler.getMethod().getReturnType() == null) {
			return;
		}
		String[] topics = handler.getEvent().returnTopics();
		if (topics.length > 0) {
			for (String t : topics) {
				eventHolder.publishEventException(t, exceptionString);
			}
		} else {
			eventHolder.publishEventException(handler.getEvent().returnTopic(), exceptionString);
		}
	}
	
	protected void invokeMethodAndPublishEvent(Event event, Message message, EventInvokeHandler handler, Object[] params, Object httpCache) {
		Exception ex = null;
		Object o = null;
		try {
			o = invokeEvent(handler, event, params, message);			
		} catch (Exception e) {
			ex = e;			
		}
		if (event.isHttpEvent()) {
			if (ex == null) {
				httpEventListener.eventFinished(httpCache, o);
			} else {
				httpEventListener.eventError(httpCache, ex);
			}
		} else {
			if (ex == null) {
				publishSuccessEvent(message, handler, o);
			} else {
				publishErrorEvent(message, handler, ex.getMessage());
			}
		}
		
	}
	
	protected Object getMessageData(Message message, Type dataType) {
		return getMessageData(message.getDataJsonString(), dataType);
	}
	
	protected Object getMessageData(String dataJsonString, Type dataType) {
		return JSONObject.parseObject(dataJsonString, dataType);
	}

	public void translateEvent(Message message) {
		postMessage(message);
	}
	
	protected void handleContext(Message message) {
		String requestId = UUIDUtil.getUid();
		ContextHolderEx.setRequestId(requestId);
		String sourceId = message.getSourceId();
		if (StringUtils.isNotBlank(sourceId)) {
			ContextHolderEx.setSourceId(sourceId);
		} else {
			ContextHolderEx.setSourceId(requestId);
		}
		ContextHolderEx.setUserId(message.getUserId());
		ContextHolderEx.setCaller(message.getCaller());
		ContextHolderEx.setCallerRequestId(message.getCallRequestId());
		ContextHolderEx.setScenario(message.getScenario());
	}
	
	private boolean scenarioCheck(Message message) {
		if (checkScenario) {
			return eventConfig.matchScenario(message.getTopic(), message.getScenario());
		}
		return true;
	}

	@Override
	public void onMessage(Message message, MessageAck ack) {
		String topic = message.getTopic();
		String msgId = message.getMsgId();	
		log.debug("##### Receive Message[{}-{}] Param[{}]", topic, msgId, message.getDataJsonString());
		log.debug("##### Message[{}-{}] CallInfo[user={}, caller={}, callerId={}, sourceId={}, scenario={}]", 
				topic, msgId, message.getUserId(), message.getCaller(), message.getCallRequestId(), 
				message.getSourceId(), message.getScenario());
		try {
			if (scenarioCheck(message)) {
				handleContext(message);
				postMessage(message);
			}
		} catch (Exception e) {
			log.error("MessageHandler.onMessage.Exception", e);
		} finally {
			ack.ack();
		}
	}
	
}
