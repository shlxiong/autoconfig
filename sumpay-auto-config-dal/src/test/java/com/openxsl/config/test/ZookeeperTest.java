package com.openxsl.config.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.alibaba.druid.util.StringUtils;
import com.openxsl.config.dal.zookeeper.ZooKeeperTemplate;
import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;

@ContextConfiguration(locations="classpath:spring/dal/http-client.xml")
@TestPropertySource(properties={"spring.autoconfig=false","spring.component.scanpackage=cn.sumpay.config.dal"})
@AutoConfig(application="springboot-test")
public class ZookeeperTest extends BasicTest{
	@Autowired
	private ZooKeeperTemplate template;
	
	@Test
	public void test() throws Exception{
		String path = "/sms|send.do";
		template.create(path, "test", false);
		
		RestURL url = new RestURL();
		url.setApplication("SMS");
		url.setDomain("manager.sumpay.cn");
		url.setContextPath("/sumpay-sms");
		url.setInstanceId("192.168.16.44:8080");
		url.setRestUri("/sms/send.do");
		System.out.println(url.toURL());
		template.save(path, url);
		
		ExecutorService pool = Executors.newFixedThreadPool(10);
		for (int i=0; i<10; i++) {
			pool.execute(() -> {
				try {
					System.out.println("ID="+template.getSequence(10));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		
		super.waitfor();
	}
	
	public class RestURL{
		private String application;  //应用名
		private String domain;       //域名
		private String instanceId;   //ip:port
		private String contextPath;  //上线文
		
		private transient String restUri;   //json不序列化
		
		public String toURL() {
			StringBuilder buffer = new StringBuilder();
			buffer.append(StringUtils.isEmpty(domain) ? instanceId : domain)
				.append(StringUtils.isEmpty(contextPath)?"":contextPath)
				.append(restUri);
			return buffer.toString();
		}
		
		public String getApplication() {
			return application;
		}
		public void setApplication(String application) {
			this.application = application;
		}
		public String getDomain() {
			return domain;
		}
		public void setDomain(String domain) {
			this.domain = domain;
		}
		public String getContextPath() {
			return contextPath;
		}
		public void setContextPath(String contextPath) {
			this.contextPath = contextPath;
		}
		public String getInstanceId() {
			return instanceId;
		}
		public void setInstanceId(String instanceId) {
			this.instanceId = instanceId;
		}
		public String getRestUri() {
			return restUri;
		}
		public void setRestUri(String restUri) {
			this.restUri = restUri;
		}
	}

}
