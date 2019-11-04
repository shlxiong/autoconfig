package com.openxsl.config.testuse;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import junit.framework.TestCase;

/**
 * 测试类的ROOT
 * @author xiongsl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
		classes={}, locations={},
		loader=AutoConfigContextLoader.class,
		initializers=AutoConfigInitializer.class 
)
@ActiveProfiles("dev")
/*@TestPropertySource( locations="*.properties",
		properties= {"key=value","key=value"}
)*/
public class BasicTest extends TestCase implements ApplicationContextAware{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private ApplicationContext context;
	
	public ApplicationContext getApplicationContext() {
		return context;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
	
	private boolean terminal = false;
	protected void waitfor() {
		while (!terminal) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}
	protected void terminate() {
		terminal = true;
	}

}
