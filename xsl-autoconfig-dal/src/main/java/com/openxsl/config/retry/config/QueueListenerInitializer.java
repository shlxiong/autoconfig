package com.openxsl.config.retry.config;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.queue.impl.MemQueue;
import com.openxsl.config.queue.impl.MemQueueListener;
import com.openxsl.config.retry.Executable;
import com.openxsl.config.retry.RetryStrategy;

/**
 * 根据Executor生成 Queue及Listener
 * @author xiongsl
 */
@ScanConfig
public class QueueListenerInitializer implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ConfigurableApplicationContext context = (ConfigurableApplicationContext)event.getApplicationContext();
		//getBeansWithAnnotation()在@Autowired之后处理
		Map<String,Object> beanMap = context.getBeansWithAnnotation(ConfigQueue.class);
		for (Map.Entry<String,Object> entry : beanMap.entrySet()) {
			this.registerQueueAndListener(entry.getValue(), entry.getKey(), context.getBeanFactory());
		}
	}
	
	private void registerQueueAndListener(Object bean, String name,
					ConfigurableBeanFactory beanFactory) throws BeansException {
		if (!(bean instanceof Executable)) {
			return;
		}
		Executable<?,?> executor = (Executable<?,?>) bean;
		ConfigQueue config = executor.getClass().getAnnotation(ConfigQueue.class);
		
		String qName = name.endsWith("Executor") ? name.replace("Executor", "Queue")
					: (name+"Queue");
		MemQueue queue;
		try {
			queue = beanFactory.getBean(qName, MemQueue.class);  //Exception
		} catch (NoSuchBeanDefinitionException ex) {
			queue = new MemQueue(qName, false);
			queue.afterPropertiesSet();
			beanFactory.registerSingleton(qName, queue);
		}
		queue.setService(executor);
		queue.setSize(config.queueSize());
		
		MemQueueListener listener = new MemQueueListener();
		listener.setConsumers(config.consumerSize());
		listener.setQueue(queue);
		if (config.nonRetry()) {
			listener.setRetry(RetryStrategy.NORETRY);
		} else {
			RetryStrategy strategy;
			String retryName = config.retryRef();
			if ("".equals(retryName)) {  //指定参数，默认跟DEFAULT的值
				strategy = new RetryStrategy(config.retries(), config.interval(), 0);
				strategy.setIncreament(config.increament());
				strategy.setTimeout(config.timeout());
			} else if ("default".equalsIgnoreCase(retryName)) {
				strategy = RetryStrategy.DEFAULT;
			} else {
				strategy = beanFactory.getBean(config.retryRef(), RetryStrategy.class);
			}
			listener.setRetry(strategy);
		}
		listener.start();
		String listenName = name.endsWith("Executor") ? name.replace("Executor", "QListener")
					: (name+"QListener");
		beanFactory.registerSingleton(listenName, listener);
	}

}
