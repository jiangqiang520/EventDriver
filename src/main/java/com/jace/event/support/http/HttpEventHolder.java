package com.jace.event.support.http;

import org.springframework.web.context.request.async.DeferredResult;

public interface HttpEventHolder {
	
	public DeferredResult<?> http2Event(String topic, Object sendObject, Object context);
	
	public Object getContext();

}
