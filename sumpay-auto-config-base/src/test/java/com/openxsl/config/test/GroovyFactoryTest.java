package com.openxsl.config.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.openxsl.config.groovy.GenericGroovyFactory;
import com.openxsl.config.groovy.GroovyInvoker;
import com.openxsl.config.groovy.ResourceGroovyFactory;
import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;

import groovy.lang.GroovyObject;

/**
 * 测试类
 * @author xiongsl
 */
@AutoConfig(application="springboot-test")
@ContextConfiguration(locations="classpath:spring-groovy.xml")
public class GroovyFactoryTest extends BasicTest {
	@Autowired
	private ResourceGroovyFactory factory;
	@Autowired
	private GenericGroovyFactory generic;
	
	@Test
	public void test() {
		GroovyObject bean = factory.getBean("Hello");
		Object says = bean.invokeMethod("sayHello", "spring-groovy");
		Assert.assertEquals("ERROR=========", "hello,spring-groovy", says);
		logger.info("{}", says);
		says = factory.getBean("HelloWorld").invokeMethod("hello", "kitty");
		Assert.assertEquals("ERROR=========", "kitty", says);
		logger.info("{}", says);
	}
	
	@Test
	public void testGeneric() throws Exception {
		generic.setSingleton(true);
		generic.update("hello", "def hello(name){return \"hello,\"+name;}");
		Object bean1 = generic.getBean("hello");
		Assert.assertNotNull("generic is null", bean1);
		Object bean2 = this.getApplicationContext().getBean("hello");
		Assert.assertEquals(bean1, bean2);
		logger.info("'hello' bean={}", bean2);
		Object says = generic.invokeMethod("hello", "hello", "kitty");
		Assert.assertEquals("ERROR=========", "hello,kitty", says);
		logger.info("{}", says);
	}
	
	@Test
	public void testInvoker() throws Exception{
		Object result;
		String filepath = "E:/openxsl/conf/springboot-test/groovy/HelloWorld.groovy";
		result = GroovyInvoker.runGroovyFile(filepath, "hello", "xiongsl");
		Assert.assertEquals("ERROR=========", "xiongsl", result);
		System.out.println("===================="+result);
		GroovyInvoker.runGroovyFile(filepath, null);  //main
		System.out.println("--------------------");
		System.out.println();
		
		filepath = "E:/openxsl/conf/springboot-test/groovy/Hello.groovy";
		result = GroovyInvoker.executeScript(filepath, "getTime", new java.util.GregorianCalendar().getTime());
		System.out.println(result);
		System.out.println();
		
		String script = "def getTime(date){return date.getTime();}\n"
				+ "def sayHello(name,age){return 'Hello,I am ' + name + ',age=' + age;}\n"
				+ "static void main(args){println('main method invoked. passby engine.eval()'); return this; }";
		result = GroovyInvoker.buildAndInvoke(script, "sayHello", new Object[] {"tomcat", 20});
		System.out.println(result);
		System.out.println("====================");
		Map<String,Object> params = new HashMap<String,Object>(2);
		params.put("name", "tomcat");
		params.put("age", 20);
		result = GroovyInvoker.buildScript(script, params);
		System.out.println(result);
	}

}
