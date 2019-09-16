package com.jace.event.core.common.util;

import com.google.common.collect.Maps;
import java.util.Map;

import org.slf4j.MDC;

public class ContextHolderEx {
    private static final String PRM_REQUEST_ID = "_requestId";
    private static final String PRM_SOURCE_ID = "_sourceId";
    private static final String PRM_CALLER = "_caller";
    private static final String PRM_USER = "_userId";
    private static final String PRM_CALLER_REQUEST_ID = "_callerRequestId";
    private static final String PRM_SCENARIO = "_scenario";

    private static ThreadLocal<Map<String, Object>> store = new ThreadLocal<>();

    public static Map<String, Object> getAll() {
        Map<String, Object> t = store.get();
        if (t == null) {
            t = Maps.newHashMap();
            store.set(t);
        }
        return t;
    }

    public static void setAll(Map<String, Object> t) {
        store.set(t);
    }

    public static Object get(String key) {
        return getAll().get(key);
    }

    public static void set(String key, String value) {
        Map<String, Object> t = store.get();
        if (t == null) {
            t = Maps.newHashMap();
            store.set(t);
        }
        t.put(key, value);
    }

    public static String getRequestId() {
        return (String)get(PRM_REQUEST_ID);
    }

    public static void setRequestId(String value) {
        set(PRM_REQUEST_ID, value);
        MDC.put("T", value);
    }
    
    public static void setScenario(String value) {
    	set(PRM_SCENARIO, value);
    }
    
    public static String getScenario() {
    	return (String)get(PRM_SCENARIO);
    }
    
    public static String getCallerRequestId() {
        return (String)get(PRM_CALLER_REQUEST_ID);
    }

    public static void setCallerRequestId(String value) {
        set(PRM_CALLER_REQUEST_ID, value);
    }

    public static String getSourceId() {
        return (String)get(PRM_SOURCE_ID);
    }

    public static void setSourceId(String value) {
        set(PRM_SOURCE_ID, value);
    }

    public static String getCaller() {
        return (String)get(PRM_CALLER);
    }

    public static void setCaller(String value) {
        set(PRM_CALLER, value);
    }

    public static String getUserId() {
        return (String)get(PRM_USER);
    }

    public static void setUserId(String value) {
        set(PRM_USER, value);
    }

    public static void clear() {
        Map<String, Object> t = store.get();
        if (t == null) {
            t = Maps.newHashMap();
            store.set(t);
        }
        t.clear();
        MDC.clear();
    }
}
