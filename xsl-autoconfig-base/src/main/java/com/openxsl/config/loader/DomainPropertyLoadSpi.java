package com.openxsl.config.loader;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DomainPropertyLoadSpi {
	private static final Logger logger = LoggerFactory.getLogger(DomainPropertyLoader.class);
	
	public static Properties loadProperties() {
		Properties props = new Properties();
		for (DomainPropertyLoader loader :
				GraceServiceLoader.loadServices(DomainPropertyLoader.class)) {
			props.putAll(loader.loadProperties());
			logger.info("{} loaded properties", loader.getClass().getName());
		}
//		Iterator<DomainPropertyLoader> itr =
//				ServiceLoader.load(DomainPropertyLoader.class).iterator();
//		while (itr.hasNext()) {
//			DomainPropertyLoader loader = itr.next();
//			boolean flag = true;
//			Autoload anno = loader.getClass().getAnnotation(Autoload.class);
//			if (anno != null) {
//				String property = anno.value();
//				flag = Environment.getProperty(property, Boolean.class, false);
//			}
//			if (flag) {
//				props.putAll(loader.loadProperties());
//				logger.info("{} loaded properties", loader.getClass().getName());
//			}
//		}
		return props;
	}

}
