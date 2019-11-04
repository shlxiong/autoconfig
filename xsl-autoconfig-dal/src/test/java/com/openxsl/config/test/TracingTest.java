package com.openxsl.config.test;

import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.thread.GrouppedThreadFactory;
import junit.framework.TestCase;

public class TracingTest extends TestCase{
	
	public void test() throws InterruptedException {
		TraceContext.initiate(null, null);  //0
		TraceContext.newRpc(new Invoker("test", "TestCase", "run"));  //0.01
		Context.setId("parent1");
		new GrouppedThreadFactory("hello").newThread(new Runnable() {  //0.01.01
			@Override
			public void run() {
//				System.out.println("child="+Context.getId());
				TraceContext.newRpc(new Invoker("pool", "inner-thread", "run"));   //0.01.01.01
				System.out.println(Thread.currentThread().hashCode()+" child-run "+TraceContext.getRpcId());
				TraceContext.popStack();
				Context.setId("child");
//				System.out.println("child="+Context.getId());
			}
			
		}).start();
//		System.out.println("parent="+Context.getId());
		TraceContext.newRpc(new Invoker("test", "TestCase", "run"));  //0.01.02
		System.out.println(Thread.currentThread().hashCode()+" parent-run "+TraceContext.getRpcId());
		Thread.sleep(1000);
//		System.out.println("parent="+Context.getId());
		TraceContext.popStack();
		TraceContext.popStack();
		while (true) {
			Thread.sleep(1000);
		}
	}
	
	
	public static class Context{
		private static ThreadLocal<String> id_ = new ThreadLocal<String>();
		
		public static String getId() {
			return id_.get();
		}
		public static void setId(String id) {
			id_.set(id);
		}
	}

}
