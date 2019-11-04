package com.openxsl.config.logger;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;

import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.joran.spi.JoranException;
import com.openxsl.config.Environment;

/**
 * @author xiongsl
 * 初始化带Spring环境变量的logback
 * copy from logback-ext-spring-0.1.4
 */
public class LogbackConfigurer {

    /**
     * Initialize logback from the given file.
     *
     * @param location the location of the config file
     *        such as "classpath:logback.xml" or "file:C:/logback.xml)
     * @throws java.io.FileNotFoundException 
     * @throws ch.qos.logback.core.joran.spi.JoranException
     */
    public static void initLogging(String location) throws FileNotFoundException, JoranException {
    	String resolvedLocation = SystemPropertyUtils.resolvePlaceholders(location);
        URL url = ResourceUtils.getURL(resolvedLocation);
        LoggerContext loggerContext = (LoggerContext)StaticLoggerBinder.getSingleton().getLoggerFactory();

        // in this version logback automatically configures at startup the context, so we have to reset it
        loggerContext.reset();

//        new ContextInitializer(loggerContext).configureByResource(url);
        JoranConfigurator configurator = new SpringBootJoranConfigurator(Environment.getSpringEnvironment());
        configurator.setContext(loggerContext);
        configurator.doConfigure(url);
        
        initJdkLogging();
    }

    /**
     * Shut down Logback.
     * 
     * This isn't strictly necessary, but recommended for shutting down
     * logback in a scenario where the host VM stays alive (for example, when
     * shutting down an application in a J2EE environment).
     */
    public static void shutdownLogging() {
    	shutdownJdkLogging();
    	
        ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
        LoggerContext loggerContext = selector.getLoggerContext();
        String loggerContextName = loggerContext.getName();
        LoggerContext context = selector.detachLoggerContext(loggerContextName);
        context.reset();
    }
    
    private static void initJdkLogging() {
    	try {
            Class<?> julBridge = ClassUtils.forName("org.slf4j.bridge.SLF4JBridgeHandler", ClassUtils.getDefaultClassLoader());
            Method removeHandlers = ReflectionUtils.findMethod(julBridge, "removeHandlersForRootLogger");
            if (removeHandlers != null) {
                ReflectionUtils.invokeMethod(removeHandlers, null);
            }
            
            Method install = ReflectionUtils.findMethod(julBridge, "install");
            if (install != null) {
                ReflectionUtils.invokeMethod(install, null);
            }
        } catch (ClassNotFoundException ignored) {
        }
    }
    private static void shutdownJdkLogging() {
    	try {
            Class<?> julBridge = ClassUtils.forName("org.slf4j.bridge.SLF4JBridgeHandler", ClassUtils.getDefaultClassLoader());
            Method uninstall = ReflectionUtils.findMethod(julBridge, "uninstall");
            if (uninstall != null) {
                ReflectionUtils.invokeMethod(uninstall, null);
            }
        } catch (ClassNotFoundException ignored) {
        }
    }

}
