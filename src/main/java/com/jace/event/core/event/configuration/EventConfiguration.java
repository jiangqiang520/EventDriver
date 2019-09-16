package com.jace.event.core.event.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.jace.event.core.event.annocationhandler.EventAnnotationBeanPostProcessor;



@Configuration
@ComponentScan({"com.dashuf.core.event"})
public class EventConfiguration {
	
	@Bean
	public EventAnnotationBeanPostProcessor eventAnnotationBeanPostProcessor() {
		return new EventAnnotationBeanPostProcessor();
	}
	
}
