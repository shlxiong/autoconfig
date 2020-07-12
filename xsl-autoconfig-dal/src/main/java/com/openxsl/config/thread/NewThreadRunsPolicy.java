package com.openxsl.config.thread;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import com.openxsl.config.thread.GrouppedThreadFactory.MyThread;

/**
 * 当线程池用尽时，新建临时线程来处理
 * @author xiongsl
 */
public class NewThreadRunsPolicy implements RejectedExecutionHandler {
	
    @Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            final Thread t = new MyThread(r, "Temporary task executor");
            t.start();
        } catch (Throwable e) {
            throw new RejectedExecutionException("Failed to start a new thread", e);
        }
    }

}
