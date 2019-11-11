package com.openxsl.config.logger.listener;

import org.slf4j.TtlMDCAdapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * LoggerContextListener是logback的上线文监听器，在初始化/终止日志前执行.
 * 此处用于修改MDC的变量
 * 
 * @author xiongsl
 */
public class TtlMdcListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {
	
	/*============= LifeCycle 3-methods ================*/
    @Override
    public void start() {
        addInfo("load TtlMDCAdapter...");
        TtlMDCAdapter.getInstance();
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    /**
     * LoggerContextListener methods
     */
    @Override
    public boolean isResetResistant() {
        return false;
    }

    @Override
    public void onStart(LoggerContext loggerContext) {

    }

    @Override
    public void onReset(LoggerContext loggerContext) {

    }

    @Override
    public void onStop(LoggerContext loggerContext) {

    }

    @Override
    public void onLevelChange(Logger logger, Level level) {

    }
}
