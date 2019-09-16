package com.jace.event.support.starter;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
//import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import com.jace.event.core.common.startup.StartupAnnotationBeanPostProcessor;
import com.jace.event.core.common.startup.StartupInvokHandler;
import com.jace.event.support.util.ContextHolder;
import com.google.common.collect.Lists;

@SuppressWarnings("deprecation")
public class Starter {
	
	private static Class<?> mainClass = null;
	
	private static String applicationName = "jace";
	
	public static Class<?> getMainClass() {
		return mainClass;
	};
	
    static {
        try {
        	//	设置系统日志级别
            Field field = LoggingSystem.class.getDeclaredField("SYSTEMS");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            Map<String, String> systems = new LinkedHashMap<>();
            systems.put("ch.qos.logback.core.Appender", "com.dashuf.core.common.logging.CustomLogbackLoggingSystem");
            systems.put("org.apache.logging.log4j.core.impl.Log4jContextFactory", "org.springframework.boot.logging.log4j2.Log4J2LoggingSystem");
            systems.put("java.util.logging.LogManager", "org.springframework.boot.logging.java.JavaLoggingSystem");

            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(LoggingSystem.class, systems);
        } catch (Exception e) {
            System.out.println("Custom log init fail!");
            e.printStackTrace();
        }
    }
    
    protected static void runStartup(ApplicationContext ct) {
    	List<StartupInvokHandler> startupList = new ArrayList<StartupInvokHandler>();
    	startupList.addAll(StartupAnnotationBeanPostProcessor.startupList);
    	Collections.sort(startupList, new  Comparator<StartupInvokHandler>() {

			@Override
			public int compare(StartupInvokHandler arg0, StartupInvokHandler arg1) {
				return arg0.getStartup().value() - arg1.getStartup().value();
			}
    		
    	});
    	for (StartupInvokHandler handler : startupList) {
    		try {
				handler.getMethod().invoke(handler.getBean());
			} catch (Exception e) {
				e.printStackTrace();
			} 
    	}
    }

    public static ConfigurableApplicationContext run(String[] args) {
        ContextHolder.set(applicationName, applicationName);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        try {
            for (StackTraceElement traceElement : stackTrace) {
                if (traceElement.getMethodName().equals("main")) {
                    mainClass = Class.forName(traceElement.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("class not found??");
            System.exit(0);
        }
        //Class<?> c = stackTrace[2].getClass().getDeclaringClass();
        System.out.println("Starting from " + mainClass.getName());
        SpringApplication app = new SpringApplication(mainClass);
        app.setBanner(new DsfBanner());
        app.addListeners(new ApplicationPidFileWriter("app.pid"));
        ConfigurableApplicationContext context = app.run(args);

        if (Lists.newArrayList(args).contains("--beans")) {
            String[] beanNames = context.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
        }
        ContextHolder.clear();
        runStartup(context);
        return context;
    }

    public static void validate() {
        if (ContextHolder.get("dsf") == null ) {
            System.out.println(StringUtils.repeat("#", 70));
            System.out.println("  在你的 main() 中使用 Starter.run 启动项目 (代替SpringApplication.run)");
            System.out.println(StringUtils.repeat("#", 70));
            System.exit(0);
        }
    }
}

class DsfBanner implements Banner {
    private String banner = "    ____             __          ____\n   / __ \\____  _____/ /_  __  __/ __/\n  / / / / __ `/ ___/ __ \\/ / / / /_\n / /_/ / /_/ (__  ) / / / /_/ / __/\n/_____/\\__,_/____/_/ /_/\\__,_/_/\n";

    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        resolveContent(sourceClass);
        out.println(banner);
    }

    private String resolveContent(Class<?> sourceClass) {
        Package pkg = (sourceClass == null ? null : sourceClass.getPackage());
        if (pkg != null) {
            banner += StringUtils.leftPad(pkg.getSpecificationTitle() + " v" + pkg.getSpecificationVersion(), 37) + "\n";
        }
        return banner;
    }
}