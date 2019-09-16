package com.jace.event.core.message.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Message {
	
	private String topic;
	
	private String scenario;
	
	private String caller;
	
	private String sourceId;
	
	private String callRequestId;
	
	private String userId;
	
	private String msgId;
	
	private String dataJsonString;
	
	private String exceptionString;

}
