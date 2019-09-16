package com.jace.event.core.http.dto;


import org.springframework.web.context.request.async.DeferredResult;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Http2EventCache {

	@NonNull
	private DeferredResult<Object> result;
	
	private Object context;
	
	@NonNull
	private String sourceId;
	
	
}
