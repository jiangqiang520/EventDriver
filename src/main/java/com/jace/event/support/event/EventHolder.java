package com.jace.event.support.event;

public interface EventHolder {
	
	void publishEvent(String topic, Object sendObject);
	
	//void publishScenario(String topic, Object sendObject);
	
	void publishEventAsJsonString(String topic, String sendJson);	
	
	//void publishScenarioAsJsonString(String topic, String sendJson);
	
	//void publishEventException(String topic, String exceptionString);
	
	//void publishEventException(String scenario, String topic, String exceptionString);
	
	//public void publishEventWithNewScenario(String scenario, String topic, Object sendObject);
	
	//public void publishEventAsJsonStringWithNewScenario(String scenario, String topic, String sendJson);
}
