package com.openxsl.config.loader;

import java.util.Properties;

/**
 * 加载配置文件的SPI接口
 * @author xiongsl
 */
public interface DomainPropertyLoader {
	
	Properties loadProperties();

}
