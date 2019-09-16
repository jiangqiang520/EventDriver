package com.jace.event.core.common.startup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import com.jace.event.support.startup.Startup;


public class StartupAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {
	
	public static List<StartupInvokHandler> startupList = new ArrayList<StartupInvokHandler>();
	

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
	
	
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Method[] methods = bean.getClass().getMethods();
		for (Method method : methods) {
			Startup startup = AnnotationUtils.findAnnotation(method, Startup.class);
			if (startup != null) {
				startupList.add(new StartupInvokHandler(bean, method, startup));
			}
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
	

}
