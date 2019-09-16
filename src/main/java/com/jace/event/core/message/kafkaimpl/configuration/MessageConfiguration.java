package com.jace.event.core.message.kafkaimpl.configuration;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
//import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import com.alibaba.fastjson.JSONObject;
import com.jace.event.core.common.inst.ServiceInstance;
import com.jace.event.core.message.dto.Message;
import com.jace.event.support.config.EnableMicroService;
import com.jace.event.support.config.EnableService;
import com.jace.event.support.starter.Starter;

import lombok.extern.slf4j.Slf4j;


@Configuration
@ComponentScan({"com.dashuf.core.message.kafkaimpl", "com.dashuf.core.message.api"})
@EnableKafka
@Slf4j
public class MessageConfiguration {
	
	private final static String NONE = "NONE";
	
	private final static String AUTO = "AUTO";
	
	@Value("${message.kafka.namesapce:AUTO}")
	private String namespace = AUTO;
	
	@Value("${message.kafka.host}")
	private String kafkaHost = null;
	
	@Value("${message.kafka.consumer.threadsCount:5}")
	private int threadsCount = 5;
	
	@Autowired
	private ServiceInstance serviceInstance = null;
	
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Integer, String>
                        kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
                                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(threadsCount);
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.setMessageConverter(new MessagingMessageConverter() {
        	protected Object extractAndConvertValue(ConsumerRecord<?, ?> record, Type type) {
        		String jsonString = (String) record.value();
        		Message message = JSONObject.parseObject(jsonString, Message.class);				
				return message;
        	}
        });
        return factory;
    }

    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
    	ConsumerFactory<Integer, String> fac = new DefaultKafkaConsumerFactory<Integer, String>(consumerConfigs());
        return fac;
    }
    
    private String determineGroupID() {    	
    	if (AUTO.equals(namespace)) {
    		Class<?> mainClass = Starter.getMainClass();
        	EnableService enableService = mainClass.getAnnotation(EnableService.class);
        	EnableMicroService enableMicroService = mainClass.getAnnotation(EnableMicroService.class);
        	if (enableService != null && enableMicroService != null) {
        		throw new RuntimeException("Can not determine [message.kafka.namesapce] because the application declare @EnableService meanwhile declare @EnableMicroService.");
        	}
        	if (enableService != null) {
        		return serviceInstance.getInstanceFullName();
        	} else {
        		return serviceInstance.getServiceName();
        	}
        } else if (NONE.equals(namespace) || StringUtils.isEmpty(namespace)) {
        	return serviceInstance.getServiceName();
        } else {
        	return serviceInstance.getServiceName() + "." + namespace;
        }	
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
    	Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, determineGroupID());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

   
	
    @Bean
    public ProducerFactory<Integer, String> producerFactory() {
        return new DefaultKafkaProducerFactory<Integer, String>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
    	Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }
	
	@Bean
    public KafkaTemplate<Integer, String> kafkaTemplate() {
        return new KafkaTemplate<Integer, String>(producerFactory());
    }

   
}
