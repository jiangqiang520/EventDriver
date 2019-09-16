package com.jace.event.support.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmptyEventContent {
	
	private static final String IDENTIFIACATION_STRING = "EMPTY_CONTENT_IDENTIFIACATION_STRING";
	
	public static EmptyEventContent INSTANCE_OBJECT = new EmptyEventContent();

	private String identificationString = IDENTIFIACATION_STRING;
	
}
