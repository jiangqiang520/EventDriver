package com.jace.event.core.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jace.event.core.common.serviceconfig.ServiceConfig;
import com.jace.event.core.event.annocationhandler.EventAnnotationBeanPostProcessor;
import com.jace.event.core.message.MessageListenTopics;
import com.jace.event.support.event.Event;
import com.jace.event.support.startup.Startup;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventConfig implements MessageListenTopics {
	
	private static final String NONE = "NONE";
	
	private List<String> listenTopics = null;
	
	private String[] listenTopicsArray = null;
	
	private List<EventConfigData> eventConfigs = null;
	
	private Map<String, EventConfigData> topicEventConfigs = new HashMap<String, EventConfigData>();
	
	@Autowired
	private ServiceConfig config = null;
	
	protected enum TOPIC_RELATION {and, or, none};
	
	@Data
	protected class EventConfigData {		
		private String[] listenTopics = null;		
		private String[] returnTopics = null;		
		private TOPIC_RELATION topicRelation = TOPIC_RELATION.none;
		private String oldScenario = null;
		private String newScenario = null;
	}
	
	protected EventConfigData generateDataFromConfig(String config) {
		EventConfigData result = new EventConfigData();
		String[] listenAndReturn = StringUtils.split(config, "->");
		String listen = listenAndReturn[0];
		if (StringUtils.contains(listen, "/")) {
			String[] listens = StringUtils.split(listen, "/");
			result.setOldScenario(listens[0].trim());
			listen = listens[1];
		}
		if (StringUtils.contains(listen, "&")) {
			result.setListenTopics(StringUtils.split(listen, "&"));
			result.setTopicRelation(TOPIC_RELATION.and);
		} else if (StringUtils.contains(listen, "|")) {
			result.setListenTopics(StringUtils.split(listen, "|"));
			result.setTopicRelation(TOPIC_RELATION.or);
		} else {
			result.setListenTopics(new String[] {listen});
			result.setTopicRelation(TOPIC_RELATION.none);
		}
		String returnT = listenAndReturn[1];
		if (StringUtils.contains(returnT, "/")) {
			String[] returnTs = StringUtils.split(returnT, "/");
			result.setNewScenario(returnTs[0].trim());
			returnT = returnTs[1];
		}
		result.setReturnTopics(StringUtils.split(returnT, ";"));
		String[] listenTopics = result.getListenTopics();
		for (int i = 0; i < listenTopics.length; i++) {
			listenTopics[i] = listenTopics[i].trim();
		}
		String[] returnTopics = result.getReturnTopics();
		for (int i = 0; i < returnTopics.length; i++) {
			returnTopics[i] = returnTopics[i].trim();
		}
		return result;
	}
	
	
	@PostConstruct
	protected void init() {
		List<String> eventConfigString = config.getConfigAsList("message.events");
		log.info("message.events.config: {}", eventConfigString);
		if (eventConfigString == null) {
			return;
		}
		listenTopics = new ArrayList<String>();
		eventConfigs = new ArrayList<EventConfigData>();
		for (String configString : eventConfigString) {
			if (!NONE.equals(configString)) {
				EventConfigData data = generateDataFromConfig(configString);
				String[] topics = data.getListenTopics();
				listenTopics.addAll(Arrays.asList(topics));
				eventConfigs.add(data);
				for (String topic : topics) {
					topicEventConfigs.put(topic, data);
				}
				
			}
		}
		listenTopicsArray = new String[listenTopics.size()];
		listenTopics.toArray(listenTopicsArray);
	}
	
	protected boolean equalsStringArray(String[] s1, String[] s2) {
		if (s1.length != s2.length) {
			return false;
		}
		for (String s : s1) {
			boolean contain = false;
			for (String ss : s2) {
				if (s.equals(ss)){
					contain = true;
				}
			}
			if (!contain) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean match(EventConfigData config, Event event) {
		if (event.listenTopic().length() > 0) {
			if (config.getTopicRelation() != TOPIC_RELATION.none || 
					!event.listenTopic().equals(config.getListenTopics()[0])) {
				return false;
			}
		} else if (event.listenTopicsAnd().length > 0) {
			if (config.getTopicRelation() != TOPIC_RELATION.and || 
					!equalsStringArray(event.listenTopicsAnd(), config.listenTopics)) {
				return false;
			}
		} else if (event.listenTopicsOr().length > 0) {
			if (config.getTopicRelation() != TOPIC_RELATION.or || 
					!equalsStringArray(event.listenTopicsOr(), config.listenTopics)) {
				return false;
			}
		}
		if (event.returnTopic().length() > 0) {
			String[] returnTopics = config.getReturnTopics();
			if (returnTopics.length != 1 || !event.returnTopic().equals(returnTopics[0])) {
				return false;
			}
		} else if (event.returnTopics().length > 0) {
			if (!equalsStringArray(config.getReturnTopics(), event.returnTopics())) {
				return false;
			}
		}
		return true;
	}
	
	public boolean match(Event event) {
		for (EventConfigData eventConfig : eventConfigs) {
			if (match(eventConfig, event)) {
				return true;
			}
		}
		return false;
	}
	
	public Event match(List<Event> events) {
		for (Event event : events) {
			if (!match(event)) {
				return event;
			}
		}
		return null;
	}
	
	public boolean matchScenario(String topic, String scenario) {
		EventConfigData config = topicEventConfigs.get(topic);
		String configScenario = config.getOldScenario();
		return (configScenario == null || configScenario.equals(scenario));
	}
	
	@Override
	public String[] getListenTopics() {
		return listenTopicsArray;
	}
	
	@Startup(Integer.MIN_VALUE)
	public void checkEvent() {
		if (!"true".equalsIgnoreCase(config.getConfig("check.event"))) {
			return;
		}
		List<Event> events = EventAnnotationBeanPostProcessor.getAllEvents();
		Event notMatchEvent = match(events);
		if (notMatchEvent != null) {
			throw new RuntimeException(EventAnnotationBeanPostProcessor.getEventKey(notMatchEvent) + " not in config!");
		}
		System.exit(0);
	}

}
