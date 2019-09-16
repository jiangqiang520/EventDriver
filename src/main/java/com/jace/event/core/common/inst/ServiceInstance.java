package com.jace.event.core.common.inst;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class ServiceInstance {
	
	//properties begin
	private String serviceName = null;
	
	private String instanceName = null;
	
	private String ip = null;
	
	private ENV env = null;
	
	private String envAsString = null;

	private String instanceFullName = null;
	
	public String getServiceName() {
		return serviceName;
	}
	
	public String getEnvAsString() {
		return envAsString;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public String getIp() {
		return ip;
	}

	public ENV getEnv() {
		return env;
	}

	public String getInstanceFullName() {
		return instanceFullName;
	}
	//properties end
	
	public enum ENV {PRD, DEV, SIT1, UAT1, SIT2, UAT2};
	
    private final static String NONE = "NONE";
    
    private final static String ENV_DEV = "DEV";	
	
	@Value("${spring.application.name:NONE}")
    private String serviceNameConfig = NONE;
    
    @Value("${frame.instance.env:DEV}")
    private String instanceEnvConfig = ENV_DEV;
	
	@Value("${frame.instance.name:NONE}")
	private String instanceNameConfig = NONE;
	
	@PostConstruct
	private void init() {
		if (NONE.equals(serviceNameConfig)) {
			throw new RuntimeException("[spring.application.name] can not be empty!");
		}
		serviceName = serviceNameConfig;
		
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException("get ip error!", e);
		}
		
		if (NONE.equals(instanceNameConfig)) {
			instanceName = ip;
			log.warn("Instance name is empty, IP[{}] will instead of it.", ip);
		} else {
			instanceName = instanceNameConfig;
		}
		
		instanceFullName = serviceName + "." + instanceName;
		
		if ("PRD".equalsIgnoreCase(instanceEnvConfig)) {
			env = ENV.PRD;
			envAsString = "PRD";
		} else if ("SIT1".equalsIgnoreCase(instanceEnvConfig)) {
			env = ENV.SIT1;
			envAsString = "SIT1";
		} else if ("UAT1".equalsIgnoreCase(instanceEnvConfig)) {
			env = ENV.UAT1;
			envAsString = "UAT1";
		} else if ("SIT2".equalsIgnoreCase(instanceEnvConfig)) {
			env = ENV.SIT2;
			envAsString = "SIT2";
		} else if ("UAT2".equalsIgnoreCase(instanceEnvConfig)) {
			env = ENV.UAT2;
			envAsString = "UAT2";
		} else if ("DEV".equalsIgnoreCase(instanceEnvConfig)) {
			env = ENV.DEV;
			envAsString = "DEV";
		} else {
			throw new RuntimeException("Can not get the env info!");
		}
	}
	

}
