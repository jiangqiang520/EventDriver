package com.jace.event.core.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.OptionHelper;

import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
//import org.springframework.boot.bind.*;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.*;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import java.nio.charset.Charset;

/**
 * 修改自{@link DefaultLogbackConfiguration}。
 * 1、将日志截断方式改为按日期，并增加配置{@code logging.policy.maxHistory}，{@code logging.policy.totalSizeCap}
 * 2、修改默认日志格式
 */
class CustomLogbackConfiguration {

    private static final String CONSOLE_LOG_PATTERN = "%date{HH:mm:ss.SSS} %clr(${LOG_LEVEL_PATTERN:-%5p}) [%X] %C{0}.%M:%L %msg%xEx%n";

    private static final String FILE_LOG_PATTERN = "%date{HH:mm:ss.SSS} %-5p [%X] %C{0}.%M:%L %msg%xEx%n";

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final PropertyResolver patterns;

    private final LogFile logFile;

    private final int maxHistory;

    private final String totalSizeCap;

    CustomLogbackConfiguration(LoggingInitializationContext initializationContext, LogFile logFile) {
        this.patterns = getPatternsResolver(initializationContext.getEnvironment());
        this.logFile = logFile;
        this.maxHistory = Integer.parseInt(initializationContext.getEnvironment().resolvePlaceholders("${logging.policy.maxHistory:30}"));
        this.totalSizeCap = initializationContext.getEnvironment().resolvePlaceholders("${logging.policy.totalSizeCap:30GB}");
    }

    private PropertyResolver getPatternsResolver(Environment environment) {
        if (environment == null) {
            return new PropertySourcesPropertyResolver(null);
        }
        return new PropertySourcesPropertyResolver((PropertySources) environment);
    }

    void apply(LogbackConfigurator config) {
        synchronized (config.getConfigurationLock()) {
            base(config);
            Appender<ILoggingEvent> consoleAppender = consoleAppender(config);
            if (this.logFile != null) {
                Appender<ILoggingEvent> fileAppender = fileAppender(config,
                        this.logFile.toString());
                config.root(Level.INFO, consoleAppender, fileAppender);
            } else {
                config.root(Level.INFO, consoleAppender);
            }
        }
    }

    private void base(LogbackConfigurator config) {
        config.conversionRule("clr", ColorConverter.class);
        config.conversionRule("wex", WhitespaceThrowableProxyConverter.class);
        config.conversionRule("wEx", ExtendedWhitespaceThrowableProxyConverter.class);
//        LevelRemappingAppender debugRemapAppender = new LevelRemappingAppender("org.springframework.boot");
//        config.start(debugRemapAppender);
//        config.appender("DEBUG_LEVEL_REMAPPER", debugRemapAppender);
        config.logger("", Level.ERROR);
        config.logger("org.apache.catalina.startup.DigesterFactory", Level.ERROR);
        config.logger("org.apache.catalina.util.LifecycleBase", Level.ERROR);
        config.logger("org.apache.coyote.http11.Http11NioProtocol", Level.WARN);
        config.logger("org.apache.sshd.common.util.SecurityUtils", Level.WARN);
        config.logger("org.apache.tomcat.util.net.NioSelectorPool", Level.WARN);
        config.logger("org.crsh.plugin", Level.WARN);
        config.logger("org.crsh.ssh", Level.WARN);
        config.logger("org.eclipse.jetty.util.component.AbstractLifeCycle", Level.ERROR);
        config.logger("org.hibernate.validator.internal.util.Version", Level.WARN);
        config.logger("org.springframework.boot.actuate.autoconfigure." + "CrshAutoConfiguration", Level.WARN);
//        config.logger("org.springframework.boot.actuate.endpoint.jmx", null, false, debugRemapAppender);
//        config.logger("org.thymeleaf", null, false, debugRemapAppender);
    }

    private Appender<ILoggingEvent> consoleAppender(LogbackConfigurator config) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        String logPattern = this.patterns.getProperty("console", CONSOLE_LOG_PATTERN);
        encoder.setPattern(OptionHelper.substVars(logPattern, config.getContext()));
        encoder.setCharset(UTF8);
        config.start(encoder);
        appender.setEncoder(encoder);
        config.appender("CONSOLE", appender);
        return appender;
    }

    @SuppressWarnings("rawtypes")
	private Appender<ILoggingEvent> fileAppender(LogbackConfigurator config, String logFile) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        String logPattern = this.patterns.getProperty("file", FILE_LOG_PATTERN);
        encoder.setPattern(OptionHelper.substVars(logPattern, config.getContext()));
        encoder.setCharset(UTF8);
        appender.setEncoder(encoder);
        config.start(encoder);

        appender.setFile(logFile);

        TimeBasedRollingPolicy timePolicy = new TimeBasedRollingPolicy();
        timePolicy.setFileNamePattern(logFile + ".%d{yyyy-MM-dd}");
        timePolicy.setMaxHistory(maxHistory);
        timePolicy.setTotalSizeCap(FileSize.valueOf(totalSizeCap));
        appender.setRollingPolicy(timePolicy);
        timePolicy.setParent(appender);
        config.start(timePolicy);

        config.appender("FILE", appender);
        return appender;
    }

}
