package com.openxsl.config.groovy;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.ScriptFactoryPostProcessor;

import groovy.lang.GroovyObject;

/**
 * 扫描指定路径下的groovy文件，并自动注册到spring容器中，beanId为文件名
 * @author xiongsl
 */
public class ResourceGroovyFactory implements ApplicationContextAware {
	private ApplicationContext context;
	private Resource[] resources;
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
						context.getAutowireCapableBeanFactory();
		for (Resource location : resources) {
			this.registerGroovyBeans(location, beanFactory);
		}
	}

	public void setLocations(Resource[] resources) {
		this.resources = resources;
	}
	
	public GroovyObject getBean(String name) {
		return (GroovyObject)context.getBean(name);
	}
	
	private void registerGroovyBeans(Resource location, DefaultListableBeanFactory beanFactory) {
		final String refreshCheckDelay = ScriptFactoryPostProcessor.REFRESH_CHECK_DELAY_ATTRIBUTE;  
//        final String language = ScriptFactoryPostProcessor.LANGUAGE_ATTRIBUTE;
        	//"org.springframework.scripting.support.ScriptFactoryPostProcessor.language";
		try {
			File[] files = location.getFile().listFiles( new FileFilter() {
			    @Override
			    public boolean accept(File pathname) {
			        return pathname.getName().endsWith(".groovy");
			    }
			});
			if (files == null) {
				return;
			}
			// <lang:groovy id="" refresh-check-delay="500" script-source="" />
			for (File file : files) {
	            RootBeanDefinition bd = new RootBeanDefinition(GroovyScriptFactory.class);
	            bd.setAttribute(refreshCheckDelay, 500);
//	            bd.setAttribute(language, "groovy");
	            // scriptSource
	            bd.getConstructorArgumentValues().addIndexedArgumentValue(0, file.toURI().toString());
	            // 注册到spring容器 
	            String beanName = file.getName().replace(".groovy", "");
	            beanFactory.registerBeanDefinition(beanName, bd);   //重名会覆盖
	        }
		} catch (FileNotFoundException e) {
			//resource.getFile()
		} catch (IOException e) {
			throw new IllegalStateException("", e);
		}
	}

}
