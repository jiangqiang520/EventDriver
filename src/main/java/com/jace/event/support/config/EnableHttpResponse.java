package com.jace.event.support.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.jace.event.core.common.configuration.CommonConfiguration;
import com.jace.event.core.http.configuration.HttpResponseConfiguration;

@SpringBootApplication
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CommonConfiguration.class, HttpResponseConfiguration.class})
public @interface EnableHttpResponse {

}
