package com.openxsl.config.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.openxsl.config.dal.RestInvoker;
import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;
import com.openxsl.config.util.KvPair;

@ContextConfiguration(locations="classpath:spring/dal/http-client.xml")
@AutoConfig(application="springboot-test")
@TestPropertySource(properties={"spring.autoconfig=false"})
public class HttpTest extends BasicTest{
	@Autowired
	private RestInvoker httpClient;
	
	@Test
	public void testHttpClient(){
//		KvPair[] pairs = {new KvPair("wd", "jacoco")};
//		httpClient.postForm("https://www.baidu.com", pairs);
		String url = "http://localhost:8080/openxsl-sms/send.do";
		KvPair[] pairs = {
				new KvPair("templateCode", "ttf_mobile_check_code"),
				new KvPair("bizId", "test"),
				new KvPair("dataId", "12344556"),
				new KvPair("bizPwd", "6f212e459bc52d0166591d6bb991aabc"),
				new KvPair("receiver", "13757180545")
		};
		try {
			System.out.println(httpClient.postForm(url, pairs));
			
			url = "https://dev.boss.openxsl.cn/framework-dfs-manage/cert/privatekey/dev/2018112718612?5e1deffc325483cb2318dd5ebe564895";
//					"https://dev.boss.openxsl.cn/framework-dfs-manage/cert/list/dev";
			System.out.println(httpClient.get(url, "", "application/json", String.class));
		}catch(Throwable t) {
			t.printStackTrace();
		}
	}

}
