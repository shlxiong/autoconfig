package com.openxsl.config;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;

import com.openxsl.config.util.StringUtils;

/**
 * 加载属性文件
 * @author xiongsl
 * @modify 2019-05-06 增加Apollo ConfigServer: getPropertySource()
 */
public class EnvironmentLoader {
	public static final String ENCODING = "UTF-8";
	//保存替换的Key <newKey, orignalKey>, Apollo使用
	static final Map<String,String> MAPPINGS = new ConcurrentHashMap<String,String>();
	
	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentLoader.class);
	private static final DefaultResourceLoader LOADER = new DefaultResourceLoader();
	private static final YamlPropertiesFactoryBean YAML = new YamlPropertiesFactoryBean();
	//属性名转换（默认驼峰，kafka需要换成"."）
	private static final ThreadLocal<String> KEY_CONVERT = new ThreadLocal<String>();
	static {
		YAML.setSingleton(false);
	}
	
	/**
	 * 如果configKey不为空，读取指定的配置文件；否则读取Environment中指定前缀的属性；
	 * 所有属性名最终转换为“驼峰”形式
	 * @param configKey  确定配置文件路径的Key
	 * @param prefix  源前缀
	 * @param targetPrefix 目标前缀
	 * @return
	 */
	public static Properties load(String configKey, String prefix, String targetPrefix) {
		Assert.notNull(prefix, "'prefix' must not be null");
		Properties props = new Properties();
		String configFile = StringUtils.isEmpty(configKey) ? null : Environment.getProperty(configKey);
		if (configFile == null) {   //直接读取Environment的值
			props = Environment.prefixProperties(prefix);
			if (targetPrefix != null) {
				for (String key : props.stringPropertyNames()) {
					String newKey =  key.replaceFirst(prefix, targetPrefix);
					String value = String.valueOf(props.remove(key)).trim();
					props.setProperty(newKey, value);
					MAPPINGS.put(newKey, key);
				}
			}
		}else {  //找文件
			String fullPath = Environment.getConfigPath() + configFile;
			try {
				props = readProperties(fullPath);
				for (String key : props.stringPropertyNames()) {  //trim
					props.setProperty(key, props.getProperty(key).trim());
				}
			} catch (IOException e) {
				LOG.info("loading from classpath, because customized does not exists: "+fullPath);
				try {
					props = PropertiesLoaderUtils.loadProperties(getResource("classpath:"+configFile));
				} catch (IOException e1) {
					throw new IllegalStateException("Properties file does not exists: "+configFile);
				}
			}
		}
		
		synchronized (KEY_CONVERT) {
			final boolean flag = (KEY_CONVERT.get() != null);
			//转换成驼峰
			for (String key : props.stringPropertyNames()) {
				if (key.contains("-")) {
					String newKey = flag ? key.replace('-', '.')
							: StringUtils.splitToCamel(key, "-");
					props.setProperty(newKey, (String)props.get(key));   //remove
					MAPPINGS.put(newKey, MAPPINGS.get(key));
				}
			}
			if (flag) {
				KEY_CONVERT.remove();
			}
		}
		return props;
	}
	
	public static Properties readProperties(String location) throws IOException {
		if (location.endsWith(".yml") || location.endsWith(".yaml")) {
			synchronized (YAML) {
				YAML.setResources(LOADER.getResource(location));
				Properties props = YAML.getObject();
				String profiles = props.getProperty("spring.profiles.active");
				if (!StringUtils.isEmpty(profiles)) {
					for (String profile : profiles.split(",")) {
						int idx = location.lastIndexOf(".");
						String details = new StringBuilder(location.substring(0, idx))
									.append("-").append(profile)
									.append(location.substring(idx))
									.toString();
						Resource resource = LOADER.getResource(details);
						if (resource.exists()) {
							YAML.setResources(resource);
							props.putAll(YAML.getObject());
						}
					}
				}
				return props;
			}
		} else {
			return PropertiesLoaderUtils.loadProperties(getResource(location));
		}
	}
	public static Properties readProperties(Resource resource) throws IOException {
		return readProperties(resource.getURL().toExternalForm());
	}
	
	public static String getOriginalKey(String newKey) {
		return MAPPINGS.get(newKey);
	}
	
	/**
	 * 标识将属性名中的“-”转换为“.”，如Kafka
	 */
	public static void setKeyConverter() {
		KEY_CONVERT.set(".");
	}
	
	static EncodedResource getResource(String location) {
		return new EncodedResource(LOADER.getResource(location), ENCODING);
	}
	static boolean exists(String location) {
		return LOADER.getResource(location).isReadable();
	}
	
	static PropertiesPropertySource getPropertySource(String location) throws IOException {
		Properties props = readProperties(location);
		//2019-05-06 config center
		ApolloConfigAdaptor.reloadProperties(props);
		
		return new PropertiesPropertySource(location, props);
//		return new ResourcePropertySource(getResource(location));
	}
	
	public static void main(String[] args) throws Exception{
//		System.out.println("spring.dubbo.registry.address".replaceFirst("spring.dubbo", "dubbo"));
//		try {
//			String location = "http://192.168.16.1/config/dev-hlw-db.properties?config.decrypt=true";
//			location = "file:/openxsl/conf/gateway/bank.properties";
//			Properties props = PropertiesLoaderUtils.loadProperties(LOADER.getResource(location));
//			for (String key : props.stringPropertyNames()) {  //trim
//				props.setProperty(key, props.getProperty(key).trim());
//			}
//			System.out.println("["+props.getProperty("citic_wx_dlrz_qrstd_md5key")+"]");
//			
//			Environment.setConfigPath("file:/openxsl/conf/gateway/");
//			Environment.addResource("file:/openxsl/conf/gateway/application.properties");
//			props = load("gateway.properties.file", "", "");
//			System.out.println("["+props.getProperty("citic_wx_dlrz_qrstd_md5key")+"]");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		String location = "file:/E:\\home\\admin\\openxsl\\deploy\\sentry\\config\\application.yml";
		readProperties(location);
	}

}
