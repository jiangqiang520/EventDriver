package com.jace.event.core.message.kafkaimpl.consume;

import com.jace.event.core.message.MessageListener;
import com.jace.event.core.message.dto.Message;
import com.jace.event.core.message.kafkaimpl.impl.MessageAckImpl;

public class MessageConsume implements Runnable {
	
	private MessageListener messageListener = null;
	
	private Message message = null;
	
	public MessageConsume(MessageListener messageListener, Message message) {
		this.messageListener = messageListener;
		this.message = message;
	}
	
	public void run() {
		messageListener.onMessage(message, new MessageAckImpl());
	}

}
