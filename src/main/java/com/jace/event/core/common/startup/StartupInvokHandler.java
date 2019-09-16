package com.jace.event.core.common.startup;

import java.lang.reflect.Method;

import com.jace.event.support.startup.Startup;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartupInvokHandler {
	
	private Object bean = null;
	
	private Method method = null;
	
	private Startup startup = null;

}
