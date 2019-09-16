package com.jace.event.core.common.serviceconfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.jace.event.core.common.inst.ServiceInstance;
import com.jace.event.core.util.FileUtil;


@Component
public class ServiceConfig {
    
    @Value("${frame.config.serviceUrl:#{null}}")
    private String configServiceUrl = null;
    
    @Value("${frame.config.connectTimeout:2000}")
	private int connectTimeout = 2000;
	
	@Value("${frame.config.readTimeout:5000}")
	private int readTimeout = 5000;
	
	@Autowired
	private ServiceInstance serviceInstance = null;
	
	private Map<String, List<String>> config = new HashMap<String, List<String>>();
	
	@PostConstruct
	private void init() {
		if (initServiceConfigFromFile()) {
			return;
		}
		if (initServiceConfigFromService()) {
			return;
		}
		init(null);
	}
	
	private void init(List<String> configData) {
		if (configData == null) {
			return;
		}
		List<String> valueList = null;
		String key = null;
		for (String line : configData) {
			line = line.trim();
			if (line.length() > 0 && !line.startsWith("#")) {
				if (line.startsWith("@")) {
					if (key != null && valueList != null && valueList.size() > 0) {
						config.put(key, valueList);
					}
					valueList = new ArrayList<String>();
					key = line.substring(1);
				} else {
					valueList.add(line);
				}
			}
		}
		if (key != null && valueList != null && valueList.size() > 0) {
			config.put(key, valueList);
		}
	}
	
	public List<String> getConfigAsList(String key) {
		return config.get(key);
	}
	
	public String getConfig(String key) {
		List<String> list = config.get(key);
		return list == null ? null : list.get(0);
	}
	
	
	private boolean initServiceConfigFromFile() {
		File file = new File(FileUtil.getCurrentPath() + "config.txt");
		if (file.exists()) {
			init(FileUtil.readFileToStringList(file, "UTF-8"));
			return true;
		} else {
			return false;
		}
		
	}
	
	private boolean initServiceConfigFromService() {
		if (configServiceUrl == null) {
			return false;
		}
		ServiceConfigRequest serviceConfigRequest = new ServiceConfigRequest();
		serviceConfigRequest.setEnv(serviceInstance.getEnvAsString());
		serviceConfigRequest.setServiceName(serviceInstance.getServiceName());
		String json = httpPost(configServiceUrl + "/api/getConfig.do", JSONObject.toJSONString(serviceConfigRequest));
		if (json == null || json.length() == 0) {
			return false;
		}
		init(JSONObject.parseArray(json, String.class));
		return true;
	}
	
	private String httpPost(String httpUrl, String post){
		try {
	        URL url = null;
            url = new URL(httpUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");// 提交模式
            httpURLConnection.setConnectTimeout(connectTimeout);//连接超时 单位毫秒
            httpURLConnection.setReadTimeout(readTimeout);//读取超时 单位毫秒
            httpURLConnection.addRequestProperty("DSF-caller", serviceInstance.getServiceName());
            httpURLConnection.setRequestProperty("Content-Type",
                    "application/json");
            httpURLConnection.setRequestProperty("Charset",
                    "UTF-8");
            // 发送POST请求必须设置如下两行
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            httpURLConnection.getOutputStream().write(post.getBytes("UTF-8"));
           
            //获取返回报文
	        StringBuffer json = new StringBuffer();
	        String line = null;
	        String result = null;
	        BufferedReader reader = null;
	        try {
	        	reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"));
	            while((line = reader.readLine()) != null) {
	                json.append(line);
	            }
	            reader.close();
	            result = json.toString();
	        } finally {
	        	if (reader != null) {
	        		reader.close();
	        	}
	        }
	        return result;	
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
	
}
