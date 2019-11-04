package com.openxsl.config.retry.config;

import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.openxsl.config.queue.impl.MemQueue;

/**
 * 注册
 * @author xiongsl
 * context.getBeansWithAnnotation(Annotation.class) IOC会强制检查依赖。
 */
public class QueueRegistrar implements ImportBeanDefinitionRegistrar {
	
	/**
	 * 先创建两个结果Queue，解决@Autowired
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Map<String,?> attributes = importingClassMetadata.getAnnotationAttributes(ConfigQueue.class.getName());
		String[] queues = (String[])attributes.get("requires");
		for (String queName : queues) {
			if (registry.containsBeanDefinition(queName)) {
				continue;
			}
			BeanDefinition definition = new RootBeanDefinition(MemQueue.class);
			definition.getPropertyValues().addPropertyValue("fair", false);
			definition.getPropertyValues().addPropertyValue("name", queName);
			registry.registerBeanDefinition(queName, definition);
		}
	}
	
}
