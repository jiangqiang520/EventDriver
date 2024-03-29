package com.jace.event.support.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreLog {
	
	boolean ignoreAll() default false;
	
	boolean ignoreInputParam() default true;
	
	boolean ignoreOutputParam() default true;

}
