package com.jace.event.core.message;

import com.jace.event.core.message.dto.Message;

public interface MessageListener  {
	
	public void onMessage(Message message, MessageAck ack);
	
}
