package com.openxsl.config.apollo;

import java.util.Collection;

import org.springframework.beans.factory.BeanFactory;

import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.spring.property.SpringValueRegistry;

import com.openxsl.config.EnvironmentLoader;
import com.openxsl.config.util.BeanUtils;

/**
 * 修改Apollo的SpringValueRegistry，该类缓存SpringValue对象，即变量与Bean的映射关系
 * @author xiongsl
 * @Create 2019-05-08
 */
public class AutoConfigValueRegistry extends SpringValueRegistry {
	private static AutoConfigValueRegistry instance = new AutoConfigValueRegistry();
	
	private AutoConfigValueRegistry() {
	}
	
	public static AutoConfigValueRegistry getInstance() {
		return instance;
	}
	
	/**
	 * 启动的时候注册，如：key=http.connect.total
	 */
	public void register(BeanFactory beanFactory, String key, SpringValue springValue) {
		//由于AutoConfig是去掉前缀的，所以需要映射一下，找到Bean中的占位符
		String orignalKey = EnvironmentLoader.getOriginalKey(key);
		String placeHolder = springValue.getPlaceholder().replace(key, orignalKey);
		BeanUtils.setPrivateField(springValue, "placeholder", placeHolder);
		super.register(beanFactory, orignalKey, springValue);
	}
	
	/**
	 * 修改的时候拉取，如：key=spring.http.connect.total
	 */
	public Collection<SpringValue> get(BeanFactory beanFactory, String key) {
		//最好的做法是AutoUpdateConfigChangeListener从 ConfigChangeEvent中取newValue
		//通过 springValue.getPlaceholder()
		return super.get(beanFactory, key);
	}

}
