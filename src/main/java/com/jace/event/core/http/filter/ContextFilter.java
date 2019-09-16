package com.jace.event.core.http.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jace.event.core.common.util.ContextHolderEx;
import com.jace.event.core.http.HeadKey;
import com.jace.event.support.util.UUIDUtil;

public class ContextFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		ContextHolderEx.clear();
		String sourceId = request.getHeader(HeadKey.HDR_SOURCE_ID);
        String caller = request.getHeader(HeadKey.HDR_CALLER);
        String callerId = request.getHeader(HeadKey.HDR_CALL_ID);
        String user = request.getHeader(HeadKey.HDR_USER);
        String requestId = UUIDUtil.getUid();
        if (StringUtils.isNotBlank(caller)) {
            ContextHolderEx.setCaller(caller);
        }
        if (StringUtils.isNotBlank(callerId)) {
            ContextHolderEx.setCallerRequestId(callerId);
        }
        if (StringUtils.isNotBlank(sourceId)) {
            ContextHolderEx.setSourceId(sourceId);
        } else {
        	ContextHolderEx.setSourceId(requestId);
        }
        if (StringUtils.isBlank(user)) {
            user = "UNKNOWN";
        }
        ContextHolderEx.setRequestId(requestId);
        
        ContextHolderEx.setUserId(user);
		try {
			filterChain.doFilter(request, response);
		} finally {
			ContextHolderEx.clear();
		}
	}

}