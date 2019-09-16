package com.jace.event.core.message.kafkaimpl.consume;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jace.event.core.message.MessageListener;
import com.jace.event.core.message.dto.Message;
import com.jace.event.core.message.kafkaimpl.KafkaMessageHandler;


@Component
public class KafkaMessageHandlerImpl implements KafkaMessageHandler {
	
	@Autowired(required = false)
	private MessageListener messageListener = null;
	
	private ThreadPoolExecutor threadPool = null;
	
	@Value("${message.minConsumeThread:5}")
	private int minConsumeThread = 5;
	
	@Value("${message.maxConsumeThread:100}")
	private int maxConsumeThread = 100;
	
	@PostConstruct
	public void init() {
		threadPool = new ThreadPoolExecutor(minConsumeThread, maxConsumeThread,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
	}

	@Override
	public void handleMessage(Message message) {
		threadPool.execute(new MessageConsume(messageListener, message));
	}

}
