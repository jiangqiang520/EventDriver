package com.jace.event.support.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import com.jace.event.core.common.configuration.CommonConfiguration;
import com.jace.event.core.event.configuration.EventConfiguration;
import com.jace.event.core.http.configuration.HttpEventConfiguration;
import com.jace.event.core.http.configuration.HttpResponseConfiguration;
import com.jace.event.core.message.kafkaimpl.configuration.MessageConfiguration;


@SpringBootApplication
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CommonConfiguration.class, EventConfiguration.class, MessageConfiguration.class, HttpResponseConfiguration.class, HttpEventConfiguration.class})
public @interface EnableService {

}
