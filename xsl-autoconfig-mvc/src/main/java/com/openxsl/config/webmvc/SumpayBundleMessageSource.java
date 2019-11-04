package com.openxsl.config.webmvc;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.openxsl.config.loader.OtherPropertiesLoader;

/**
 * ResourceBundle(去除前缀)
 * @author xiongsl
 */
public class SumpayBundleMessageSource extends ReloadableResourceBundleMessageSource {
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	
	@Override
	protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {
		Resource resource = this.resourceLoader.getResource(filename + ".properties");
		if (!resource.exists()) {
			resource = this.resourceLoader.getResource(filename + ".xml");
		}
		
		try {
			Properties props = this.loadProperties(resource, filename);
			//cacheMills: 缓存时间      refreshTimestamp：刷新时间
			long refreshTimestamp = (getCacheMillis() < 0 ? -1 : System.currentTimeMillis());
			propHolder = new PropertiesHolder(props, refreshTimestamp);
		} catch (IOException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not parse properties file [" + resource.getFilename() + "]", ex);
			}
			propHolder = new PropertiesHolder();
		}
		
//		this.cachedProperties.put(filename, propHolder);
		return propHolder;
	}

	/**
	 * 优先加载 /openxsl/conf/application.properties下的本工程属性
	 */
	protected Properties loadProperties(Resource resource, String filename) throws IOException {
		Properties props = OtherPropertiesLoader.loadProperties();
		if (props.isEmpty()) {
			return super.loadProperties(resource, filename);
		}else {
			return props;
		}
	}

}
