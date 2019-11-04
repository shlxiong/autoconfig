package com.openxsl.config.autodetect;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.openxsl.config.Environment;
import com.openxsl.config.util.StringUtils;

/**
 * 自动扫描组件（spring.autoconfig=true开启扫描“com.openxsl.config”包）
 * @author xiongsl
 */
public class AutoConfigRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
	//AnnotationBeanNameGenerator只认 @Component
	private final BeanNameGenerator beanNameGenerator = new InnerBeanNameGenerator();
	private ConfigurableEnvironment environ;
	private String packages;
	
	public AutoConfigRegistryPostProcessor(ConfigurableEnvironment environ) {
		this.environ = environ;
		final String autoPkg = "com.openxsl.config";
		packages = Environment.getProperty(Environment.SCAN_SWITCH, Boolean.class, true)
				 ? autoPkg : "";   //自动创建 @ScanConfig对象
		String custPkgs = Environment.getProperty(Environment.COMPONENT_SCAN_PACKAGE);
		if (!StringUtils.isEmpty(custPkgs)) {
			packages = "".equals(packages) || autoPkg.startsWith(custPkgs) ? //packages="cn.openxsl"
					custPkgs : (packages + ", "+custPkgs);
		}
		if (packages.length() > 0) {
			System.out.println("    scan annotation packages: " + packages);
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		if (packages.length() == 0) {
			return;
		}
		
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
		scanner.setEnvironment(environ);
		scanner.setIncludeAnnotationConfig(true);
		scanner.setBeanNameGenerator(beanNameGenerator);
		//防止springboot扫描到
		scanner.addIncludeFilter(new AnnotationTypeFilter(ScanConfig.class));
		//Controller交给 mvc容器
		scanner.addExcludeFilter(new AnnotationTypeFilter(Controller.class));
		scanner.addExcludeFilter(new AnnotationTypeFilter(ControllerAdvice.class));
		scanner.scan(StringUtils.split(packages, ","));
	}
	
	class InnerBeanNameGenerator extends AnnotationBeanNameGenerator {
		
//		public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry){
//		}
		protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
			AnnotationMetadata amd = annotatedDef.getMetadata();
			Set<String> types = amd.getAnnotationTypes();  //所有注解
			for (String type : types) {
				Map<String, Object> attributes = amd.getAnnotationAttributes(type, false);
				if (ScanConfig.class.getName().equals(type) 
						|| isStereotypeWithNameValue(type, amd.getMetaAnnotationTypes(type), attributes)) {
					Object value = attributes.get("value");
					if (value != null && value instanceof String) {
						String strValue = (String)value;
						if (!StringUtils.isEmpty(strValue)) {
							return strValue;
						}
					}
				}
			}
			return null;
		}
	}

}
