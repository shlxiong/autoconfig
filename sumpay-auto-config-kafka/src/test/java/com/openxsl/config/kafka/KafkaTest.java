package com.openxsl.config.kafka;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import com.openxsl.config.kafka.core.ProducerListener;
import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;

@AutoConfig(application="springboot-test")
public class KafkaTest extends BasicTest{
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private KafkaTemplate<String, Object> template;
	
	@Test
//	@Transactional(rollbackFor = RuntimeException.class)
	public void test() {
		String topic = "test-topic";
		template.send(topic, "This is a test message")
			.addCallback(new ProducerListener(topic, logger));
		super.waitfor();
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String[] args) {
		KafkaTemplate<String, Object> template =
				new ClassPathXmlApplicationContext("classpath:application.xml")
					.getBean(KafkaTemplate.class);
		template.send("test-topic", "This is a test message").addCallback(new SuccessCallback<Object>() {
			@Override
			public void onSuccess(Object arg0) {
				System.out.println("发送成功");
			}
		}, new FailureCallback() {
			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
			}
		});
	}

}
