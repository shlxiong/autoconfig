package com.openxsl.config.test;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.tracing.TraceAsynContext;
import com.openxsl.config.filter.tracing.TraceContext;

import junit.framework.TestCase;

public class TraceContextTest extends TestCase{
	
	public void _testContext() {
		TraceContext.initiate("0", null);    //0
		{
			TraceContext.newRpc(new Invoker("unkown", "Main", "m1"));  //0.01
			System.out.println("m1   "+TraceContext.getRpcId());
			{
				TraceContext.newRpc(new Invoker("unkown", "Inner1", "m1-1"));
				System.out.println("m1-1 "+TraceContext.getRpcId());
				TraceContext.popStack();
				
				TraceContext.newRpc(new Invoker("unkown", "Inner1", "m1-2"));
				System.out.println("m1-2 "+TraceContext.getRpcId());
				{
					TraceContext.newRpc(new Invoker("unkown", "Inner2", "m1-2-1"));
					System.out.println("m1-2-1 "+TraceContext.getRpcId());
					TraceContext.popStack();
				}
				TraceContext.popStack();
			}
			TraceContext.popStack();
			
			TraceContext.newRpc(new Invoker("unkown", "Main", "m2"));
			System.out.println("m2   "+TraceContext.getRpcId());
			{
				TraceContext.newRpc(new Invoker("unkown", "Inner3", "m2-1"));
				System.out.println("m2-1 "+TraceContext.getRpcId());
				TraceContext.popStack();
			}
			TraceContext.popStack();
		}
	}
	
	public void testAsynContext() {
		TraceAsynContext.initiate("1");
		ExecutorService pool = TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(5));
		for (int i=0; i<5; i++) {
			pool.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					Thread.sleep(new Random().nextInt(2000));
					String name = Thread.currentThread().getName();
					TraceAsynContext.newRpc(new Invoker("unkown", name, "run"));
					System.out.println(name+"-e  "+TraceAsynContext.innerRpc().getRpcId());
					ExecutorService pool2 = Executors.newFixedThreadPool(5); //TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(5));
					for (int j=0; j<5; j++) {
						pool2.submit(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(new Random().nextInt(1000));
								} catch (InterruptedException e) {
								}
								String name = "CC-"+Thread.currentThread().getName();
								TraceAsynContext.newRpc(new Invoker("unkown", name, "run"));
								System.out.println(name+"-e  "+TraceAsynContext.innerRpc().getRpcId());
								TraceAsynContext.clear();
							}
						});
					}
					pool2.shutdown();
					return null;
				}
			});
			TraceAsynContext.newRpc(new Invoker("unkown", "", "run"));
		}
		pool.shutdown();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("root=" + TraceAsynContext.innerRpc().getRpcId());  //0
		TraceAsynContext.clear();
	}

}
