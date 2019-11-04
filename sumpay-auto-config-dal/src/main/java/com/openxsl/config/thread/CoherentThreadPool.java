package com.openxsl.config.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 测试的时候，使用多线程执行某个方法
 * 
 * @author xiongsl
 */
public class CoherentThreadPool {
	private final int size;
	private final ExecutorService executors;
	
	public static void execute(int size, Runnable runnable) {
		new CoherentThreadPool(size).execute(runnable);
	}
	
	public CoherentThreadPool(int size) {
		executors = new ThreadPoolExecutor(size, size, 60, TimeUnit.SECONDS,
							new LinkedBlockingQueue<Runnable>(),
							new GrouppedThreadFactory("CoherentThreadPool"));
		this.size = size;
	}
	
	public void execute(Runnable runnable) {
		for (int i=0; i<size; i++) {
			executors.execute(runnable);
		}
		try {
			executors.awaitTermination(60000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
