package com.jace.event.core.http.configuration;

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jace.event.core.common.util.ContextHolderEx;
import com.jace.event.core.event.EventHolderEx;
import com.jace.event.core.event.httpevent.HttpEventListener;
import com.jace.event.core.http.dto.Http2EventCache;
import com.jace.event.core.message.dto.Message;
import com.jace.event.support.http.HttpEventHolder;

@Configuration
public class HttpEventConfiguration {
	
	@Value("${frame.http.eventTimeoutMillis:5000}")
    public int eventTimeoutMillis = 5000;
	
    private Cache<String, Http2EventCache> lacalCache;

    @PostConstruct
    public void init() {
        lacalCache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .concurrencyLevel(10)
                .expireAfterWrite(eventTimeoutMillis + 1000, TimeUnit.MILLISECONDS)
                .build();
    }
	
	@Bean
	public HttpEventListener httpEventListener() {
		return new HttpEventListener() {
			
			@Override
			public void eventFinished(Object cache, Object eventReturn) {
				Http2EventCache c = (Http2EventCache)cache;
				DeferredResult<Object> o = c.getResult();
				o.setResult(eventReturn);
				lacalCache.invalidate(c.getSourceId());
			}

			@Override
			public void eventError(Object cache, Exception e) {
				Http2EventCache c = (Http2EventCache)cache;
				DeferredResult<Object> o = c.getResult();
				o.setErrorResult(e);
			}

			@Override
			public Object canInvoke(Message message) {
				Http2EventCache c = lacalCache.getIfPresent(message.getSourceId());
				return c;
			}
		};
//		return new HttpEventListener() {

//			@Override
//			public Object canInvoke(Message message) {
//				Http2EventCache c = lacalCache.getIfPresent(message.getSourceId());
//				return c;
//			}
//
//			@Override
//			public void eventFinished(Object cache, Object eventReturn) {
//				Http2EventCache c = (Http2EventCache)cache;
//				DeferredResult<Object> o = c.getResult();
//				o.setResult(eventReturn);
//				lacalCache.invalidate(c.getSourceId());
//			}
//
//			@Override
//			public void eventError(Object cache, Exception e) {
//				Http2EventCache c = (Http2EventCache)cache;
//				DeferredResult<Object> o = c.getResult();
//				o.setErrorResult(e);
//			}
			
//		};
	}
	
	@Bean
	public HttpEventHolder httpEventHolder(EventHolderEx eventHolder) {
		return new HttpEventHolder() {
			@Override
			public DeferredResult<?> http2Event(String topic, Object sendObject, Object context) {
				eventHolder.publishScenario(topic, sendObject);
				String sourceId = ContextHolderEx.getSourceId();
				DeferredResult<Object> result = new DeferredResult<Object>((long) eventTimeoutMillis);
				Http2EventCache c = new Http2EventCache(result, sourceId);
				c.setContext(context);
				lacalCache.put(sourceId, c);
				return result;
			}

			@Override
			public Object getContext() {
				Http2EventCache c = lacalCache.getIfPresent(ContextHolderEx.getSourceId());
				return c.getContext();
			}
		};
	}

}
