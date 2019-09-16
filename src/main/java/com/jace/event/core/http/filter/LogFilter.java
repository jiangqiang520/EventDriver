package com.jace.event.core.http.filter;

import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import com.alibaba.fastjson.JSONObject;
import com.jace.event.core.common.util.ContextHolderEx;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogFilter extends OncePerRequestFilter {

    protected boolean includeParam = true;
        
    public LogFilter() {
    	super();
    }    
    
    private String trimTo1000(String s) {
    	if (s != null && s.length() > 1000) {
    		return s.substring(0, 997) + "...";
    	}
    	return s;
    }
    
    private String getBody(HttpServletRequest request) {
    	return null;
    }
    
    private String toPrintParam(HttpServletRequest request) {
    	String body = getBody(request);
    	Map<String, String[]> param = request.getParameterMap();
    	StringBuffer toPrint = new StringBuffer();
    	if (body != null && body.length() > 0) {
    		toPrint.append("body='").append(trimTo1000(body)).append("' " );
    	}
    	if (param != null && !param.isEmpty()) {
    		toPrint.append("param='").append(trimTo1000(request.getParameterMap().toString())).append("'");
    	}
    	return toPrint.toString();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        WrappedHttpResponse custResponse = WrappedHttpResponse.wrap(response);
        long beg = System.currentTimeMillis();
        String uri = request.getRequestURI();
        if (includeParam) {
            log.info("##### Begin Request[{}] Param[{}]", uri, toPrintParam(request));
        } else {
            log.info("##### Begin Request[{}]", uri);
        }
        log.info("##### Request[{}] CallInfo[user={}, caller={}, callerId={}, sourceId={}]", uri, ContextHolderEx.getUserId(), ContextHolderEx.getCaller(), ContextHolderEx.getCallerRequestId(), ContextHolderEx.getSourceId());
        try {
            filterChain.doFilter(request, custResponse);
        } catch (RuntimeException e) {
        	log.error("##### Exception in Request[" + uri + "]", e);
        	throw e;
        } finally {
            long end = System.currentTimeMillis();
            log.info("##### Finish Request[{}] Cost[{}ms] Response[{}]", uri, end - beg, trimTo1000(custResponse.getResponseContent()));
            //ContextHolderEx.clear();
        }
    }
}
