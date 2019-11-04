package com.openxsl.config;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.openxsl.config.autodetect.AutoConfigRegistryPostProcessor;
import com.openxsl.config.util.StringUtils;

/**
 * Web的环境变量
 * @author xiongsl
 */
public final class Environment {
	public static final String ROOT_CONTEXT = "spring.context.root";
	public static final String COMPONENT_SCAN_PACKAGE = "spring.component.scanpackage";
	public static final String SCAN_SWITCH = "spring.autoconfig";
	
	private static final StandardServletEnvironment ENVIRON = new StandardServletEnvironment();
	private static final XmlWebApplicationContext SPRING_CONTEXT;
	private static Properties MERGED_PROPERTIES = new Properties();
	private static String application, configPath;
	private static String owner, address;
	
	static {
		ENVIRON.getPropertySources().addFirst(
				new PropertiesPropertySource("CONVERSION", MERGED_PROPERTIES));
		String profiles = ENVIRON.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
		if (!StringUtils.isEmpty(profiles)) {
			ENVIRON.setActiveProfiles(profiles.split(","));
		}
		SPRING_CONTEXT = BeanUtils.instantiate(XmlWebApplicationContext.class);
		SPRING_CONTEXT.setEnvironment(ENVIRON);
	}
	
	public static StandardServletEnvironment getSpringEnvironment() {
		return ENVIRON;
	}
	public static XmlWebApplicationContext getSpringContext() {
		return SPRING_CONTEXT;
	}
	public static void setSpringContextLoader() {
		SPRING_CONTEXT.addBeanFactoryPostProcessor(new AutoConfigRegistryPostProcessor(ENVIRON));
	}
	
	/**配置文件的路径*/
	public static void setConfigPath(String path) {
		configPath = path;
	}
	public static String getConfigPath() {
		if (configPath == null) {
			throw new IllegalStateException("Pleanse specify 'spring.config.location'");
		}
		return configPath;
	}
	/**应用名*/
	public static String getApplication() {
		return application;
	}
	public static void setApplication(String application) {
		Environment.application = application;
	}
	/**应用负责人 */
	public static String getOwner() {
		return owner;
	}
	public static void setOwner(String owner) {
		Environment.owner = owner;
	}
	/**IP+端口*/
	public static String getAddress() {
		return address;
	}
	public static void setAddress(String address) {
		Environment.address = address;
	}
	
	/**
	 * 是否为Spring-boot环境
	 */
	public static boolean existSpringBoot() {
		return exists("org.springframework.boot.SpringApplication");
	}
	public static final boolean exists(String... classes) {
		final ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
		for (String clazz : classes) {
			if (ClassUtils.isPresent(clazz, classLoader)) {
				return true;
			}
		}
		return false;
	}
	
	public static void addResource(String location) {
		try {
			ENVIRON.getPropertySources().addAfter("CONVERSION", //会按顺序处理
						EnvironmentLoader.getPropertySource(location));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String property, String... defaultValue) {
		if (defaultValue.length > 0) {
			return ENVIRON.getProperty(property, defaultValue[0]);
		}else {
			return ENVIRON.getProperty(property);
		}
		//return SystemPropertyUtils.resolvePlaceholders(value);
	}
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(String property, Class<T> type, T... defaultValue) {
		T value = ENVIRON.getProperty(property, type);
		if (value==null && defaultValue.length>0) {
			return (T)defaultValue[0];
		}
		return value;
	}
	
	public static String getSystemProperty(String property, String... defaultValue) {
		Object value = ENVIRON.getSystemProperties().get(property);
		if (value == null) {
			if (defaultValue.length > 0) {
				return defaultValue[0];
			}
			return null;
		}else {
			return value.toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Properties prefixProperties(String prefix) {
		Properties target = new Properties();
		Set<String> keySet;
		for (PropertySource<?> source : ENVIRON.getPropertySources()) {
			if (source.getSource() instanceof Map) {  //systemProperties, systemEnv, or Mapped
				keySet = ((Map<String,?>)source.getSource()).keySet();
				for (String key : keySet) {
					if (key.startsWith(prefix) && !target.containsKey(key)) {
						target.setProperty(key,  ENVIRON.getProperty(key));
					}
				}
			}
		}
		return target;
	}
	
	public static void mergeProperties(Properties properties) {
		MERGED_PROPERTIES.putAll(properties);
	}
	
	public static int getSize() {
		return ENVIRON.getPropertySources().size();
	}
	
	public static boolean hasProperty(String property) {
		return !StringUtils.isEmpty(ENVIRON.getProperty(property));
	}
	public static void copy2SystemProps(String property) {
		String value = ENVIRON.getProperty(property, "");
		if (value.length() > 0) {
			System.setProperty(property, value);
		}
	}

}
