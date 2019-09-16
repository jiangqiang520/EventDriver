package com.jace.event.core.http.configuration;


import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.jace.event.core.common.inst.ServiceInstance;
import com.jace.event.core.common.util.ContextHolderEx;
import com.jace.event.core.http.HeadKey;

import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;

@Configuration
public class HttpRequestConfiguration {
	
	@Autowired
	private ServiceInstance serviceInstance = null;
	
	@Value("${frame.http.request.connectTimeoutMillis:2000}")
    public int requestConnectTimeoutMillis = 2000;
	
	@Value("${frame.http.request.readTimeoutMillis:5000}")
    public int requestReadTimeoutMillis = 5000;
	
	@Value("${frame.http.request.retryCount:0}")
    public int requestRetryCount = 0;
	
	@Bean
	public RequestInterceptor contextRequestInterceptor() {
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate template) {
				String sourceId = ContextHolderEx.getSourceId();
	        	if (StringUtils.isBlank(sourceId)) {
	        		sourceId = ContextHolderEx.getRequestId();
	            }
	            template.header(HeadKey.HDR_SOURCE_ID, sourceId);
	            template.header(HeadKey.HDR_USER, ContextHolderEx.getUserId());
	            template.header(HeadKey.HDR_CALLER, serviceInstance.getServiceName());
	            template.header(HeadKey.HDR_CALL_ID, ContextHolderEx.getRequestId());
			}
			
		};
	}
	
	@Bean
    Request.Options feignOptions() {
        return new Request.Options(requestConnectTimeoutMillis, requestReadTimeoutMillis);
    }
	
	@Bean
    Retryer feignRetryer() {
		if (requestRetryCount <= 0) {
			return Retryer.NEVER_RETRY; 
		} else {
			return new Retryer.Default(100, requestReadTimeoutMillis, requestRetryCount);
		}
    }

}
