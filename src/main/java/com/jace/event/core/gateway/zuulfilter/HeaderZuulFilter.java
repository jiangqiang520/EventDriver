package com.jace.event.core.gateway.zuulfilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.jace.event.core.common.inst.ServiceInstance;
import com.jace.event.core.common.util.ContextHolderEx;
import com.jace.event.core.http.HeadKey;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

@Component
public class HeaderZuulFilter extends ZuulFilter {
	
	@Autowired
	private ServiceInstance serviceInstance = null;

	public boolean shouldFilter() {
		return true;
	}

	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
    	ctx.addZuulRequestHeader(HeadKey.HDR_SOURCE_ID, ContextHolderEx.getSourceId());
    	ctx.addZuulRequestHeader(HeadKey.HDR_USER, ContextHolderEx.getUserId());
    	ctx.addZuulRequestHeader(HeadKey.HDR_CALLER, serviceInstance.getServiceName());
    	ctx.addZuulRequestHeader(HeadKey.HDR_CALL_ID, ContextHolderEx.getRequestId());
		return null;
	}

	public String filterType() {
		return "pre";
	}

	public int filterOrder() {
		return 0;
	}

}
