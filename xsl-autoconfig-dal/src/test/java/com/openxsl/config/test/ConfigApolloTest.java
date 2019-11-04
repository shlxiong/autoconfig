package com.openxsl.config.test;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;

@AutoConfig(application="springboot-test")
@TestPropertySource(properties={"spring.autoconfig=false"})
@ContextConfiguration(locations="classpath:spring/dal/http-client.xml")
public class ConfigApolloTest extends BasicTest {
	@Autowired
	private PoolingHttpClientConnectionManager httpConnMgr;
	
	@Test
	public void refresh() {
		while (true) {
			System.out.println("http maxTotal:====="+httpConnMgr.getMaxTotal());
			try {
				Thread.sleep(30 * 1000);
			} catch (Exception e) {
				
			}
		}
	}

}
