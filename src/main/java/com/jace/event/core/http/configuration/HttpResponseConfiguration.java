package com.jace.event.core.http.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jace.event.core.http.filter.ContextFilter;
import com.jace.event.core.http.filter.LogFilter;



@Configuration
public class HttpResponseConfiguration {
	
	@Bean
    public FilterRegistrationBean contextFilterBean() {
        FilterRegistrationBean bean = new FilterRegistrationBean(new ContextFilter());
        bean.setOrder(-1000);
        bean.addUrlPatterns("*.do","/api/*");
        return bean;
    }
	
	@Bean
    public FilterRegistrationBean logFilterBean() {
        FilterRegistrationBean bean = new FilterRegistrationBean(new LogFilter());
        bean.setOrder(-500);
        bean.addUrlPatterns("*.do","/api/*");
        return bean;
    }
	
}
