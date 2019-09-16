package com.jace.event.core.message.kafkaimpl;

import com.jace.event.core.message.dto.Message;

public interface KafkaMessageHandler {
	
	public void handleMessage(Message message);

}
