package com.openxsl.config.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.Assert;

/**
 * 由于配置中加了前缀'spring.'，解决Mybatis扫描包
 * @author xiongsl
 */
public class MybatisMapperScanner extends MapperScannerConfigurer {
	private ApplicationContext applicationContext;
	private String scanPkg;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
	    this.applicationContext = applicationContext;
	    super.setApplicationContext(applicationContext);
	}
	@Override
	public void setBasePackage(String basePackage) {
	    this.scanPkg = basePackage;
	    super.setBasePackage(basePackage);
	}
	
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		Assert.notNull(scanPkg, "还未设置属性'basePackage'");
		if (scanPkg.startsWith("${")) {
			this.processBasePackages();
		} else {
			super.setBasePackage(scanPkg);
		}
		
		super.setProcessPropertyPlaceHolders(false);
		super.postProcessBeanDefinitionRegistry(registry);
	}
	
	private void processBasePackages() {
		List<PropertyResourceConfigurer> configurers = new ArrayList<PropertyResourceConfigurer>(
					applicationContext.getBeansOfType(PropertyResourceConfigurer.class).values());
		if (applicationContext instanceof AbstractApplicationContext) {
			List<BeanFactoryPostProcessor> postProcessors =
					((AbstractApplicationContext)applicationContext).getBeanFactoryPostProcessors();
			for (BeanFactoryPostProcessor postProcessor : postProcessors) {
				if (postProcessor instanceof PropertyResourceConfigurer) {
					configurers.add((PropertyResourceConfigurer)postProcessor);
				}
			}
		}
		
		String basePackage = null;
		for (PropertyResourceConfigurer configurer : configurers) {
			basePackage = this.getBasePackages(configurer);
			if (basePackage != null) {
				super.setBasePackage(basePackage);
			}
		}
		Assert.notNull(basePackage, "未找到环境变量:"+scanPkg);
	}
	
	private String getBasePackages(PropertyResourceConfigurer configurer) {
		final String propName = scanPkg.substring(2, scanPkg.length()-1);
		Properties[] properties = (Properties[])this.getPrivateField(configurer, "localProperties");
		for (Properties props : properties) {
			String value = props.getProperty(propName);
			if (value!=null && value.trim().length()>0) {
				return value;
			}
		}
		return null;
	}
	
	private Object getPrivateField(Object bean, String field) {
		Object value = null;
		Field f = null;
		Class<?> beanClass = bean.getClass();
		while (f == null) {
			try {
				boolean flag = true;
				try{
					f = beanClass.getField(field);
				} catch (NoSuchFieldException nf) {
					f = beanClass.getDeclaredField(field);
					if (!f.isAccessible()) {
						flag = false;
						f.setAccessible(true);
					}
				}
				if (f != null) {
					value = f.get(bean);
					f.setAccessible(flag);
				}
			}catch(Exception e){
				beanClass = beanClass.getSuperclass();
			}
		}
		Assert.notNull(f, "addsdfsdf");
		return value;
	}
	
	public static void main(String[] args) {
		MybatisMapperScanner test = new MybatisMapperScanner();
		//test.setBasePackage("${mybatis.mapper.scanpackage}");
		Object bean = new PropertyPlaceholderConfigurer();
		test.getPrivateField(bean, "localProperties");
	}

}
