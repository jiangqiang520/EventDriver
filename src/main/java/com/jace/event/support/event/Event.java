package com.jace.event.support.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Event {

	String returnTopic() default "";

	String listenTopic() default "";
	
	String[] listenTopicsOr() default {};
	
	String[] listenTopicsAnd() default {};
	
	String[] returnTopics() default {};
	
	int cacheSecond() default 5;
	
	boolean isHttpEvent() default false;

}
