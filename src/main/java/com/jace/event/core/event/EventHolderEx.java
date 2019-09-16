package com.jace.event.core.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.jace.event.core.common.inst.ServiceInstance;
import com.jace.event.core.common.util.ContextHolderEx;
import com.jace.event.core.message.MessageHolder;
import com.jace.event.core.message.dto.Message;
import com.jace.event.support.event.EventHolder;
import com.jace.event.support.util.UUIDUtil;


@Component
public class EventHolderEx implements EventHolder {
	
	@Autowired
	private ServiceInstance serviceInstance = null;
	
	@Autowired
	private MessageHolder messageHolder;
	
	protected Message generateCommonMessage() {
		Message msg = new Message();
		msg.setCaller(serviceInstance.getServiceName());
		String requestId = ContextHolderEx.getRequestId();
		msg.setCallRequestId(requestId);
		msg.setMsgId(UUIDUtil.getUid());
		String sourceId = ContextHolderEx.getSourceId();
		if (sourceId == null) {
			sourceId = requestId;
		}
		msg.setSourceId(sourceId);
		msg.setUserId(ContextHolderEx.getUserId());
		return msg;
	}
	
	protected void sendMessage(String topic, Object sendObject) {
		sendMessageAsJsonString(topic, JSONObject.toJSONString(sendObject));
	}
	
	protected void sendMessage(String scenario, String topic, Object sendObject) {
		sendMessageAsJsonString(scenario, topic, JSONObject.toJSONString(sendObject));
	}
	
	protected void sendException(String topic, String exceptionString) {
		Message msg = generateCommonMessage();
		msg.setDataJsonString(null);
		msg.setTopic(topic);
		msg.setExceptionString(exceptionString);
		msg.setScenario(ContextHolderEx.getScenario());
		messageHolder.sendMessage(msg);
	}
	
	public void sendException(String scenario, String topic, String exceptionString) {
		Message msg = generateCommonMessage();
		msg.setDataJsonString(null);
		msg.setTopic(topic);
		msg.setExceptionString(exceptionString);
		msg.setScenario(scenario);
		messageHolder.sendMessage(msg);
	}

	
	public void sendMessageAsJsonString(String topic, String sendJson) {
		Message msg = generateCommonMessage();
		msg.setDataJsonString(sendJson);
		msg.setTopic(topic);
		msg.setExceptionString(null);
		msg.setScenario(ContextHolderEx.getScenario());
		messageHolder.sendMessage(msg);
	}

	
	public void sendMessageAsJsonString(String scenario, String topic, String sendJson) {
		Message msg = generateCommonMessage();
		msg.setDataJsonString(sendJson);
		msg.setTopic(topic);
		msg.setExceptionString(null);
		msg.setScenario(scenario);
		messageHolder.sendMessage(msg);
	}
	

	@Override
	public void publishEvent(String topic, Object sendObject) {
		sendMessage(topic, sendObject);
	}

	@Override
	public void publishEventAsJsonString(String topic, String sendJson) {
		sendMessageAsJsonString(topic, sendJson);
	}
	
	//@Override
	public void publishEventWithNewScenario(String scenario, String topic, Object sendObject) {
		sendMessage(scenario, topic, sendObject);
	}
	
	//@Override
	public void publishEventAsJsonStringWithNewScenario(String scenario, String topic, String sendJson) {
		sendMessageAsJsonString(scenario, topic, sendJson);
	}

	//@Override
	public void publishScenario(String topic, Object sendObject) {
		sendMessage(topic, topic, sendObject);
	}

	//@Override
	public void publishScenarioAsJsonString(String topic, String sendJson) {
		sendMessageAsJsonString(topic, topic, sendJson);
	}

	//@Override
	public void publishEventException(String topic, String exceptionString) {
		sendException(topic, exceptionString);
	}
	
	//@Override
	public void publishEventException(String scenario, String topic, String exceptionString) {
		sendException(scenario, topic, exceptionString);
	}
	
}
