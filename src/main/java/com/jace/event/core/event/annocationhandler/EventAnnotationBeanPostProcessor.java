package com.jace.event.core.event.annocationhandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import com.jace.event.support.event.Event;
import com.jace.event.support.event.EventAPI;
import com.jace.event.support.event.ReturnEvents;
import com.jace.event.support.event.TopicParam;
import com.jace.event.support.log.IgnoreLog;



public class EventAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {
	
	private static Map<String, EventInvokeHandler> eventMethods = new HashMap<String, EventInvokeHandler>();
	
	private static Map<String, String[]> topicParams = new HashMap<String, String[]>();
	
	public static EventInvokeHandler getEventInvokeHandler(String topic) {
		return eventMethods.get(topic);
	}
	
	public static List<Event> getAllEvents() {
		List<Event> result = new ArrayList<Event>();
		Collection<EventInvokeHandler> tmp = eventMethods.values();
		for (EventInvokeHandler handle : tmp) {
			result.add(handle.getEvent());
		}
		return result;
	}
	
	public static String[] getTopicParams(Event event) {
		return getTopicParams(getEventKey(event));
	}
	
	public static String[] getTopicParams(String eventKey) {
		return topicParams.get(eventKey);
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
	
	/*public Set<String> getAllEventTopics() {
		return eventMethods.keySet();
	}*/
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (AnnotationUtils.findAnnotation(bean.getClass(), EventAPI.class) != null) {
			Method[] methods = bean.getClass().getMethods();
			for (Method method : methods) {
				Event event = AnnotationUtils.findAnnotation(method, Event.class);
				if (event != null) {
					IgnoreLog ignoreLog = AnnotationUtils.findAnnotation(method, IgnoreLog.class);
					putEventMethods(event, bean, method, ignoreLog);
				}
			}
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
	public static String getEventKey(Event event) {
		if (event.listenTopic().length() > 0) {
			return event.listenTopic();
		}
		else {
			String[] topics = null;
			String linkString = null;
			StringBuffer sb = new StringBuffer();
			if (event.listenTopicsAnd().length > 0) {
				topics = event.listenTopicsAnd();
				linkString = "&";
			} else {
				topics = event.listenTopicsOr();
				linkString = "|";
			}
			for (String t : topics) {
				sb.append(t).append(linkString);
			}
			int last = sb.length() - 1;
			sb.delete(last, sb.length());
			return sb.toString();
		}
	}
	
	protected void putEventMethods(Event event, Object bean, Method method, IgnoreLog ignoreLog) {
		List<String> topics = checkAndGetListenTopics(event);
		checkReturnTopics(event, method);
		for (String topic : topics) {
			if (eventMethods.containsKey(topic)) {
				throw new RuntimeException("event topic[" + topic + "] are duplicate!");
			}
			eventMethods.put(topic, new EventInvokeHandler(bean, method, event, ignoreLog));
		}
		if (event.listenTopic().length() == 0) {
			topicParams.put(getEventKey(event), checkAndGetTopicParams(topics, method));
		}
	}
	
	protected String[] checkAndGetTopicParams(List<String> topics, Method method) {
		String[] topicParamsAnn = new String[topics.size()];
		List<String> topicParamsAnnList = new ArrayList<String>();
		Annotation[][] allParamAnns = method.getParameterAnnotations();
		for (Annotation[] oneParamAnns : allParamAnns) {
			for (Annotation paramAnn : oneParamAnns) {
				if (paramAnn instanceof TopicParam) {
					String topic = ((TopicParam) paramAnn).value();
					if (topicParamsAnnList.contains(topic)) {
						throw new RuntimeException("topicParam[" + topic + "] are not duplicate!");
					}
					topicParamsAnnList.add(topic);
				}
			}
		}
		if (topicParamsAnnList.size() != topics.size()) {
			throw new RuntimeException("event" + topics +" topicParam are not available!");
		}
		return topicParamsAnnList.toArray(topicParamsAnn);
	}
	
	protected List<String> checkAndGetListenTopics(Event event) {
		List<String> result = new ArrayList<String>();
		boolean topicAvailable = false;
		if (event.listenTopic().length() > 0) {
			if (topicAvailable) {
				throw new RuntimeException("event[" + getEventKey(event) + "] topic are not available!");
			}
			result.add(event.listenTopic());
			topicAvailable = true;
		}
		if (event.listenTopicsAnd().length > 0) {
			if (topicAvailable) {
				throw new RuntimeException("event[" + getEventKey(event) + "] topic are not available!");
			}
			Collections.addAll(result, event.listenTopicsAnd());
			topicAvailable = true;
		}
		if (event.listenTopicsOr().length > 0) {
			if (topicAvailable) {
				throw new RuntimeException("event[" + getEventKey(event) + "] topic are not available!");
			}
			Collections.addAll(result, event.listenTopicsOr());
			topicAvailable = true;
		}
		if (topicAvailable) {
			return result;
		} else {
			throw new RuntimeException("event[" + getEventKey(event) + "] topic are not available!");
		}
	}
	
	protected void checkReturnTopics(Event event, Method method) {
		String[] topics = event.returnTopics();
		String topic = event.returnTopic();
		Class<?> returnClass = method.getReturnType();
		if ("void".equals(returnClass.getName())) {
			if (topics.length == 0 && topic.length() == 0) {
				return;
			} else {
				throw new RuntimeException("event[" + getEventKey(event) + "] returnTopic(s) are not available!");
			}
		}
		if (event.isHttpEvent()) {
			if (topics.length > 0 || topic.length() > 0) {
				throw new RuntimeException("event[" + getEventKey(event) + "] returnTopic(s) are not available!");
			}
			return;
		}
		if (ReturnEvents.class.isAssignableFrom(returnClass)) {
			if (topics.length == 0) {
				throw new RuntimeException("event[" + getEventKey(event) + "] returnTopic(s) are not available!");
			}
			if (topic.length() != 0) {
				throw new RuntimeException("event[" + getEventKey(event) + "] returnTopic(s) are not available!");
			}
		} else {
			if (topics.length > 0) {
				throw new RuntimeException("event[" + getEventKey(event) + "] returnTopic(s) are not available!");
			}
			if (topic.length() == 0) {
				throw new RuntimeException("event[" + getEventKey(event) + "] returnTopic(s) are not available!");
			}
		}
	}
	


}
