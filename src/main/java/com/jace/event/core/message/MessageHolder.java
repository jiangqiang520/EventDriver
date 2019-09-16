package com.jace.event.core.message;

import com.jace.event.core.message.dto.Message;

public interface MessageHolder {
	
	//void sendMessage(String topic, Object sendObject);
	
	//void sendMessageAsJsonString(String topic, String sendJson);
	
	//void sendException(String topic, String exceptionString);
	
	//void sendMessage(String scenario, String topic, Object sendObject);
	
	//void sendMessageAsJsonString(String scenario, String topic, String sendJson);
	
	//void sendException(String scenario, String topic, String exceptionString);
	
	void sendMessage(Message message);

}
