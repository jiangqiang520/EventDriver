package com.jace.event.core.message.kafkaimpl.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.jace.event.core.message.MessageHolder;
import com.jace.event.core.message.dto.Message;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MessageHolderImpl implements MessageHolder {
	
	@Autowired
	private KafkaTemplate<Integer, String> template;
	
	public void sendMessage(Message msg) {
		template.send(msg.getTopic(), JSONObject.toJSONString(msg));
		log.debug("##### Send Message[{}-{}] Param[{}]", msg.getTopic(), msg.getMsgId(), msg.getExceptionString() == null ? msg.getDataJsonString() : msg.getExceptionString());
	}

}
