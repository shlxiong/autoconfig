package com.openxsl.config.rocketmq.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.rocketmq.client.producer.LocalTransactionExecuter;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.common.message.Message;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import com.openxsl.config.rocketmq.MessageListenerConfiguration;
import com.openxsl.config.rocketmq.ProducerConfiguration;
import com.openxsl.config.rocketmq.RocketProperties;
import com.openxsl.config.rocketmq.core.RocketTemplate;
import com.openxsl.config.rocketmq.core.RocketTemplate.DefaultSendCallback;
import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;

/**
 * @author xiongsl
 */
@ContextConfiguration(
		locations="classpath*:spring/dal/http-client.xml",
		classes= {RocketProperties.class,
				ProducerConfiguration.class, MessageListenerConfiguration.class
		}
)
@AutoConfig(application="springboot-test")
public class RocketmqTest extends BasicTest {
	@Autowired(required=false)
	private RocketTemplate template;
	
	@Test
	public void test() throws Exception{
		if (template != null) {  //生产者
			//如果既有broadcasting又cluster，则cluster不会接收到消息
			String msgId = null;
			msgId = template.send("topic_xiongsl", "Hello, world!", null);
			Assert.notNull(msgId, "发送失败");
			logger.info("Already Sent");
			
			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("TAG", "key_A");
			headers.put("user.directory", "file:/openxsl/paycore"+System.currentTimeMillis());
			msgId = template.send("topic_xiong2", "Hello, Tomcat!", headers);
			Assert.notNull(msgId, "发送失败");
			logger.info("Already Sent");
			
			headers.put("DELAY", 3);  //延时：1s,5s,10s,30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
			headers.put("TAG", "delay");
			msgId = template.send("topic_broadcast", "Hello, every one!", headers);
			Assert.notNull(msgId, "发送失败");
			logger.info("Already Sent");
		}
	}
	
//	@Test
	public void testBatch() throws Exception{
		final int len = 2048;
		List<Message> messages = new ArrayList<Message>(len);
		for (int i=0; i<len; i++) {
			byte[] body = String.format("Hello, every one!->%d", i).getBytes();
			messages.add(new Message("topic_broadcast", body));
		}
		try {
			template.sendBatch("topic_broadcast", messages, 2000);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
//	@Test
	public void testFilter() throws Exception{
		Map<String, Object> headers = new HashMap<String, Object>();
		//通过Tag过滤(OK)
		String[] keys = {"key_A", "key_B", "key_C", "key_D"};
		for (int i = 0; i < 12; i++){
			headers.clear();
			headers.put("TAG", keys[i%4]);
			template.send("topic_xiong2", "Hello, Tomcat!", headers);
			logger.info("Already Sent");
		}
		//bySQL92
		headers.put("TAG", "TagA");
		headers.put("var1", 1);
		template.send("TopicTest", "Hello, SQL-Filter!", headers);
		//byCode
		headers.put("KEYS", "Hello KEY");
		template.send("TopicFilter", "Hello, Source-Filter!", headers);
	}
	
//	@Test
	public void testAsync() throws Exception{
		template.asyncSend("topic_xiongsl", "Hello, Tomcat! oneway", null, null);
		logger.info("Already Sent");
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("TAG", "key_A");
		SendCallback callback = new DefaultSendCallback("key_A");
		template.asyncSend("topic_xiong2", "Hello, Tomcat!", headers, callback);
		logger.info("Already Sent");
		String[] topics = {"topic_xiongsl", "topic_xiong2", "topic_broadcast"};
		headers.put("TAG", "key_C");
		template.sendTopics(topics, "the same message for multi-topics", headers);
	}
	
//	@Test
	public void testTransaction() throws Exception {
		String topic = "topic_tranx";
		Map<String, Object> headers = new HashMap<String, Object>();
		//确定什么情况下提交或回滚
		LocalTransactionExecuter executor = new LocalTransactionExecuter() {
			@Override
			public LocalTransactionState executeLocalTransactionBranch(Message msg, Object arg) {
				Integer seq = Integer.parseInt(msg.getUserProperty("sequence"));
				if (seq % 3 == 0) {
					return LocalTransactionState.COMMIT_MESSAGE;
				} else if (seq % 3 == 2){
					return LocalTransactionState.ROLLBACK_MESSAGE;
				} else {
					//return LocalTransactionState.UNKNOW;
					throw new RuntimeException("Unknow-Fail");
				}
			}
		};
		for (int i = 0; i < 12; i++){
			headers.put("sequence", Integer.valueOf(i));
			template.sendTransactional(topic, "trans_msg_"+i, headers, executor);
		}
	}
	
	@After  //等待消费消息
	public void waitfor() {
		while (true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	static Pattern pattern = Pattern.compile("\\w+((\\s)*\\|\\|(\\s)*\\w+)*");
	public static void main(String[] args) {
		Matcher m = pattern.matcher("key_A || key_C");
		System.out.println(m.matches());
	}
	
}
