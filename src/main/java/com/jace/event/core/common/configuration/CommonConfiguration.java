package com.jace.event.core.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.jace.event.core.common.startup.StartupAnnotationBeanPostProcessor;
import com.jace.event.support.starter.Starter;


@Configuration
@ComponentScan({"com.jace.event.core.common"})
public class CommonConfiguration {
	
    static {
        Starter.validate();
    }
	
	@Bean
	public StartupAnnotationBeanPostProcessor startupAnnotationBeanPostProcessor() {
		return new StartupAnnotationBeanPostProcessor();
	}
	
	
	

	
	

}
