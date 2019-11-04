package com.openxsl.config.test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;

import com.openxsl.config.thread.GrouppedThreadFactory;

@AutoConfig(application="springboot-test")
@ContextConfiguration(locations="classpath*:none.xml")
@TestPropertySource(properties={"spring.autoconfig=false"})
public class ThreadFactoryTest extends BasicTest {
	
	@Test
	public void test() {
		ScheduledExecutorService pool = new GrouppedThreadFactory("sch-test").newScheduledPool(1);
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				System.out.println("invoked. "+Thread.currentThread());
			}
		};
		
		//ScheduledThreadPoolExecutor
		pool.scheduleAtFixedRate(runner, 10, 2, TimeUnit.SECONDS);
		
		waitfor();
	}

}
