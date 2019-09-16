package com.jace.event.core.common.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;

/**
 * 继承自{@link LogbackLoggingSystem}，替换默认配置
 */
public class CustomLogbackLoggingSystem extends LogbackLoggingSystem {
    public CustomLogbackLoggingSystem(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected void loadDefaults(LoggingInitializationContext initializationContext, LogFile logFile) {
        LoggerContext context = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
        stopAndReset(context);
        LogbackConfigurator configurator = new LogbackConfigurator(context);
        context.putProperty("LOG_LEVEL_PATTERN", initializationContext.getEnvironment().resolvePlaceholders("${logging.pattern.level:${LOG_LEVEL_PATTERN:%5p}}"));
        new CustomLogbackConfiguration(initializationContext, logFile).apply(configurator);
        context.setPackagingDataEnabled(true);
    }

    private void stopAndReset(LoggerContext loggerContext) {
        loggerContext.stop();
        loggerContext.reset();
        if (isBridgeHandlerAvailable()) {
            addLevelChangePropagator(loggerContext);
        }
    }

    private void addLevelChangePropagator(LoggerContext loggerContext) {
        LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
        levelChangePropagator.setResetJUL(true);
        levelChangePropagator.setContext(loggerContext);
        loggerContext.addListener(levelChangePropagator);
    }
}
