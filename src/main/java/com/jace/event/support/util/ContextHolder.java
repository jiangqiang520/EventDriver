package com.jace.event.support.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jace.event.core.common.util.ContextHolderEx;


public class ContextHolder {

	public static Object get(String key) {
		return ContextHolderEx.get(key);
	}

	public static void set(String key, String value) {
		if (key.startsWith("_")) {
			throw new RuntimeException("不能使用以_为开关的Key");
		}
		ContextHolderEx.set(key, value);
	}

	public static String getRequestId() {
		return ContextHolderEx.getRequestId();
	}

	public static String getUserId() {
		return ContextHolderEx.getUserId();
	}

	public static void setUserId(String value) {
		ContextHolderEx.setUserId(value);
	}
	
	public static String getCaller() {
		return ContextHolderEx.getCaller();
	}
	
	public static void clear() {
		Map<String, Object> map = ContextHolderEx.getAll();
		Set<String> keySet = map.keySet();
		List<String> deleteKeyList = new ArrayList<String>();
		for (String s : keySet) {
			if (!s.startsWith("_")) {
				deleteKeyList.add(s);
			}
		}
		for (String s : deleteKeyList) {
			map.remove(s);
		}
	}
	
	public static String getCallerRequestId() {
        return ContextHolderEx.getCallerRequestId();
    }
	
	public static String getScenario() {
		return ContextHolderEx.getScenario();
	}
	
	public static String getSourceId() {
		return ContextHolderEx.getSourceId();
	}
}
