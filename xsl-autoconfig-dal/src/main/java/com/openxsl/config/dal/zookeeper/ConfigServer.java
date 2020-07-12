package com.openxsl.config.dal.zookeeper;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import com.openxsl.config.util.CollectionUtils;
import com.openxsl.config.util.StringUtils;

/**
 * Zookeeper配置中心
 * @author xiongsl
 */
public class ConfigServer {
	private final ZkPropertyConfigurer configurer = new ZkPropertyConfigurer();
	private final ConversionService converter =	DefaultConversionService.getSharedInstance();
	private String path;
	
	public ConfigServer(String localFile, String zkpath) {
		try {
			configurer.setLocalFiles(localFile);  //initiate ZkPropertyConfigurer
		} catch(IOException e) {
			throw new IllegalStateException("ConfigServer can't be initated", e);
		}
		
		this.setWorkingPath(zkpath);
	}
	
	public void setWorkingPath(String zkpath) {
		this.path = zkpath;
		List<String> locations = new ArrayList<String>(4);
		int idx = -1;
		while ((idx=path.indexOf("/", idx+1)) != -1) {
			locations.add(path.substring(0, idx));
		}
		locations.add(path);
		configurer.setLocations(CollectionUtils.array(locations));
	}

	public Properties getProperties() {
		return configurer.getProperties();
	}
	
	/**
	 * 取 name的属性值，name可能包含路径，会逐级去匹配
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name, Class<T> prototype, T... defaultValue) {
		String value = this.getProperty(name);
		if (value != null) {
			return converter.convert(value, prototype);
		} else {
			return (defaultValue.length > 0) ? defaultValue[0] : null;
		}
	}
	
	public String getProperty(String name, String... defaultValue) {
		String value = configurer.getProperties().getProperty(name);
		if (value == null) {
			String prefix, key;
			int idx = -1;
			while ((idx=path.indexOf("/", idx+1)) != -1) {
				prefix = path.substring(0, idx).replace('/', '.') + '.';
				if (!name.startsWith(prefix)) {
					//不包含前缀，则肯定不存在
					break;
				}
				key = name.substring(prefix.length());
				if (configurer.getProperties().containsKey(key)) {
					value = configurer.getProperties().getProperty(key);
				}
			}
			if (value == null && defaultValue.length > 0) {
				value = defaultValue[0];
			}
		}
		return value;
	}
	
	public <T> T getPropertyBean(String subPath, Class<T> clazz) {
		Properties props = configurer.getRemoteProperties(subPath);
		Properties localProps = configurer.getLocalProperties();
		T bean = org.springframework.beans.BeanUtils.instantiate(clazz);
		Field field;
		for (PropertyDescriptor desc : org.springframework.beans.BeanUtils.getPropertyDescriptors(clazz)) {
			String value, name = desc.getName();
			try {
				field = clazz.getDeclaredField(name);
			} catch (NoSuchFieldException | SecurityException e) {
				continue;
			}
			if (field.isAnnotationPresent(Value.class)) {  //指定了属性名
				String[] values = this.getFieldAnnotation(field);
				name = values[0];
				value = this.getProperties().getProperty(name, values[1]);
			} else {
				String camel = StringUtils.splitToCamel(name, "-");
				value = props.getProperty(name, props.getProperty(camel));
				if (value == null) {
					name = String.format("%s.%s", path.replace("/", "."),name);
					camel = String.format("%s.%s", path.replace("/", "."),camel);
					value = localProps.getProperty(name, localProps.getProperty(camel));
				}
			}
			if (value != null) {
				Object valueObj = converter.convert(value, field.getType());
				try {
					desc.getWriteMethod().invoke(bean, valueObj);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return bean;
	}

	public ZkPropertyConfigurer getConfigurer() {
		return configurer;
	}
	
	private String[] getFieldAnnotation(Field field) {
		String name = field.getName();
		String defaultValue = null;
		name = field.getAnnotation(Value.class).value();
		if (name.startsWith("${")) {
			name = name.substring(2, name.length()-1);
		}
		int idx = name.indexOf(":");
		if (idx != -1) {
			defaultValue = name.substring(idx+1);
			name = name.substring(0, idx);
		}
		return new String[]{name, defaultValue};
	}
	
}
