package com.openxsl.config.test;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.MergedContextConfiguration;

import com.openxsl.config.Environment;
import com.openxsl.config.EnvironmentLoader;
import com.openxsl.config.loader.DubboPropertiesLoader;
import com.openxsl.config.loader.MvcPropertiesLoader;
import com.openxsl.config.loader.OtherPropertiesLoader;
import com.openxsl.config.loader.RedisPropertiesLoader;
import com.openxsl.config.loader.RocketPropertiesLoader;
import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.AutoConfigContextLoader;
import com.openxsl.config.testuse.BasicTest;
import com.openxsl.config.util.SpringRegistry;
import com.openxsl.config.util.Version;

@ContextConfiguration(
		//locations="classpath:spring/dal/http-client.xml",
//		loader=PropertiesLoaderTest.InnerContextLoader.class
		)
@AutoConfig(application="springboot-test")
public class PropertiesLoaderTest extends BasicTest{
//	static {
//		TestContextManager.prepareTestInstance();
//	}
	
	@Autowired
	private TestBean test;
	
	@Test
	public void testLoadDataSource() {
		Properties props = DubboPropertiesLoader.loadProperties();
		Assert.assertNotNull("Can't find dubboProps", props.get("dubbo.application.name"));
		
		props = MvcPropertiesLoader.loadProperties();
		Assert.assertEquals(".html", props.getProperty("freemarker.suffix"));
		props = OtherPropertiesLoader.loadProperties();
		Assert.assertEquals("kitty", props.getProperty("project.hello"));
		props = RedisPropertiesLoader.loadProperties();
		Assert.assertNotNull("Can't find redisProps", props.get("redis.host"));
		props = RocketPropertiesLoader.loadProperties();
		Assert.assertNotNull("Can't find rocketProps", props.get("rocketmq.nameServer"));
	}
	
//	@Test
	public void testPrefixProperties() {
		System.out.println(test.getJdbcProperties());
		System.out.println(test.getVerifyProperties());
		System.out.println(test.getVerifyPropertiesWithPrefix());
		System.out.println(test.getAllProperties());
	}
	
//	@Test
	public void testVersion() {
		String path = "D:/eclipse/workspace/dubbo-rpc-api-2.8.1-20170630.015942-5.jar";
		path = "D:/eclipse/workspace/dubbo-rpc-api-2.8.1-SNAPSHOTS.jar";
		path = "dubbo-rpc-api-2.8.1-20170630.015942-5.jar";
		String version = Version.getJarVersion(path);
		Assert.assertEquals(version, "2.8.1-20170630.015942-5");
		version = Version.getVersion(org.slf4j.Logger.class, "1.0");
		Assert.assertEquals(version, "1.7.25");
		
		Version.expectVersion(ApplicationContext.class, "4.3.8.SNAPSHOT", true);
		Version.expectVersionIfExist("com.alibaba.dubbo.common.URL", "2.5.7", true);
		
		//Version.hasResource("log4j-1.2*.jar", true);
		Version.hasResource("Version.class", true);
	}
	
	public static class InnerContextLoader extends AutoConfigContextLoader{
		@Override
		protected void prepareContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
			Environment.addResource("file:/openxsl/conf/springboot-test/application.properties");
			Properties props = EnvironmentLoader.load(null, "spring.http", "http");
			SpringRegistry.addPlaceholderConfigurer(props, context);
			super.prepareContext(context, mergedConfig);
		}
	}

}
