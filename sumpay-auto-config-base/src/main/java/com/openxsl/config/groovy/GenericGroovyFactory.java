package com.openxsl.config.groovy;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Base64Utils;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 * 执行Groovy代码的Factory
 * @author xiongsl
 */
public class GenericGroovyFactory implements ApplicationContextAware{
	// cache-script of <beanName, md5(script)>
	private final Map<String, String> scriptMap = new ConcurrentHashMap<String, String>();
	private final Map<String, Class<?>> loadedClassMap = new ConcurrentHashMap<String, Class<?>>();
	private DefaultListableBeanFactory beanFactory;
	private boolean singleton;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
	}
	public void setSingleton(boolean flag) {
		this.singleton = flag;
	}
	
	/**
	 * 取得(singleton)或创建Groovy对象
	 */
	public GroovyObject getBean(String beanName) throws Exception {
		if (singleton) {
			return (GroovyObject)beanFactory.getBean(beanName);
		}
		
		try {
			return (GroovyObject)loadedClassMap.get(beanName).newInstance();
		} catch(NullPointerException npe) {
			throw new IllegalArgumentException("can't find any groovy-class for bean '"+beanName
					+ "', please invoke update(String,String) before it.");
		}
	}
	
	/**
	 * 更新Groovy脚本
	 */
	public void update(String beanName, String script) throws IOException {
		boolean flag = true;
		String key = md5(script);
		if (scriptMap.containsKey(beanName)) {
			flag = !scriptMap.get(beanName).equals(key);
		}
		if (flag) {
			scriptMap.put(beanName, key);
			Class<?> beanClass = this.loadScriptClass(script);
			loadedClassMap.put(beanName, beanClass);
			if (singleton) {
				beanFactory.destroySingleton(beanName);
				try {
					beanFactory.registerSingleton(beanName, beanClass.newInstance());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Object invokeMethod(String beanName, String method, Object args) throws Exception{
		GroovyObject bean = this.getBean(beanName);
		return bean.invokeMethod(method, args);
	}
	
	private Class<?> loadScriptClass(String script) throws IOException{
		Class<?> clazz = null;
		GroovyClassLoader groovyClassLoader = GroovyInvoker.getGroovyClassLoader();
		try {
			//产生“script_timestamp_hashcode.groovy”的脚本对象（PermGen）
			clazz = groovyClassLoader.parseClass(script);
		}finally {
			if (groovyClassLoader != null) {
				groovyClassLoader.close();
			}
		}
		return clazz;
	}
	
	private static String md5(String str) {
		try {
		    MessageDigest md = MessageDigest.getInstance("MD5");
		    byte[] b1 = md.digest(str.getBytes("UTF-8"));
		    return Base64Utils.encodeToString(b1);
		} catch(Exception e) {
		    return str;
		}
	}

}
