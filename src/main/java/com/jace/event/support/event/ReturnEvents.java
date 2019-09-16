package com.jace.event.support.event;

import java.util.HashMap;
import java.util.Map;

public class ReturnEvents {
	
	private Map<String, Object> returnObjects = new HashMap<String, Object>();
	
	public void add(String topic, Object returnObject) {
		returnObjects.put(topic, returnObject);
	}
	
	public Object getReturnObject(String topic) {
		return returnObjects.get(topic);
	}
	
	public void clear() {
		returnObjects.clear();
	}

}
