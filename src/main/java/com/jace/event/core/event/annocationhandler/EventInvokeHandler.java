package com.jace.event.core.event.annocationhandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.jace.event.support.event.Event;
import com.jace.event.support.log.IgnoreLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventInvokeHandler {
	
	@NonNull
	private Object bean;
	
	@NonNull
	private Method method;
	
	@NonNull
	private Event event;
	
	private IgnoreLog ignoreLog = null;
	
	
	public Object invoke(Object[] params)  {
		try {
			return method.invoke(bean, params);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
