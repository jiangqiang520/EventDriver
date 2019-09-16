package com.jace.event.core.message.api;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jace.event.core.common.inst.ServiceInstance;
import com.jace.event.core.message.MessageHolder;
import com.jace.event.core.message.dto.Message;
import com.jace.event.support.util.ResponseInfo;
import com.jace.event.support.util.UUIDUtil;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MessageAPI {
	
	@Autowired
	private MessageHolder messageHolder = null;
	
	@Autowired
	private ServiceInstance serviceInstance = null;	
	
	@RequestMapping("message/ping")
	public ResponseInfo<String> send() {
		try {
			Message msg = new Message();
			msg.setCaller(serviceInstance.getServiceName());
			String requestId = UUIDUtil.getUid();
			msg.setCallRequestId(requestId);
			msg.setMsgId(UUIDUtil.getUid());
			msg.setSourceId(requestId);
			msg.setUserId(null);
			msg.setDataJsonString("\"PING_STRING\"");
			msg.setTopic("Ping");
			msg.setExceptionString(null);
			msg.setScenario("Ping");
			messageHolder.sendMessage(msg);
			return ResponseInfo.success("OK-" + new Date());
		} catch (Exception e) {
			log.error("send message error!", e);
			return ResponseInfo.error(e.getMessage());
		}
	}
	
	@KafkaListener(topics = {"Ping"})
	public void handleMessage(Message msg) {
		if (serviceInstance.getServiceName().equals(msg.getCaller())) {
			log.info("##### Ping Message[{}]", msg);
		}
	}

}
