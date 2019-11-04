package com.openxsl.config.dal.zookeeper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;

import com.openxsl.config.dal.zookeeper.ZooKeeperTemplate.ZkDataListener;
import com.openxsl.config.dal.zookeeper.impl.AbstractZKTemplate;
import com.openxsl.config.dal.zookeeper.impl.CuratorTemplate;
import com.openxsl.config.dal.zookeeper.impl.ZkClientTemplate;
import com.openxsl.config.loader.PrefixProperties;
import com.openxsl.config.util.BeanUtils;
import com.openxsl.config.util.StringUtils;

/**
 * Load-from and store-to Zookeeper server.
 * '/configsvr/${location1}
 * '/configsvr/${location2}
 * 
 * @author 001327-xiongsl
 * 
PropertyResourceConfigurer{ >> PropertiesLoaderSupport ->>BeanFactoryPostProcessor
    void postProcessBeanFactory{
        Properties mergedProps = mergeProperties(){
            for (Resource location : this.locations) {  //loadProperties();
	            //DefaultPropertiesPersister
	            this.propertiesPersister.load(props, new EncodedResource(location));
            }
            //if (localOverride) localProp to mergedProps else mergedProps to local
	    }
	    convertProperties(mergedProps);   //无实际从操作
	    processProperties(beanFactory, mergedProps);   //将属性放到beanFactory(StringValueResolver)
    }
}
 */
public class ZkPropertyConfigurer extends PropertyPlaceholderConfigurer
				implements ZkDataListener{
	public static final String ZK_SERVER_LIST = "spring.zookeeper.address";
	public static final String ZK_AUTHORITY = "spring.zookeeper.authority";
	public static final String ZK_CLIENT_API = "spring.zookeeper.client.api";
	public static final String ZK_NAMESPACE = "spring.zookeeper.namespace";
	public static final String ROOT_PATH = "/configsvr";
	
	private static Map<String, ZkPropertiesPersister> peristerMap
				= new HashMap<String, ZkPropertiesPersister>(4);
	
	private final ZkResourceLoader loader = new ZkResourceLoader();
	private ZkPropertiesPersister persister;
	private Properties properties;
	private Map<String, Properties> layeredProperties = new HashMap<String, Properties>();

	/**
	 * 本地配置文件，一些必要的设置（spring.zookeeper.address）
	 */
	public void setLocalFiles(String... files) throws IOException{
		Properties localFileProps = new Properties();
		for (String file : files){
			localFileProps.putAll(PrefixProperties.get(file, "spring.zookeeper", false));
		}
		super.setProperties(localFileProps);   //->super.localProperties
		
		String servers = localFileProps.getProperty(ZK_SERVER_LIST);
		String authority = localFileProps.getProperty(ZK_AUTHORITY);
		String api = localFileProps.getProperty(ZK_CLIENT_API, "zkclient");
		String rootPath = localFileProps.getProperty(ZK_NAMESPACE, ROOT_PATH);
		if (servers != null){
			String key = String.format("%s_%s", servers,api);
			persister = peristerMap.get(key);
			if (persister == null) {
				persister = new ZkPropertiesPersister();
				persister.setZookeeperTemplate(this.getZookeeperTemplate(api, servers, authority, rootPath));
			}
			super.setPropertiesPersister(persister);
		}
	}
	
	/**
	 * 加载（监听）ZK指定目录的内容
	 * @param locations Zookeeper路径
	 */
	public void setLocations(String[] locations){
		Assert.notEmpty(locations, "zkpath 'locations' must be NotEmpty");
		Assert.notNull(persister, "Please assure property 'localFile' is not null,"
				+ " and the item 'spring.zookeeper.address' is setted");
		
		properties = null;
		layeredProperties.clear();
		
		Resource[] resources = new Resource[locations.length];
		int i = 0;
		for (String location : locations){
			if (location.startsWith("${") && location.endsWith("}")){
				location = location.substring(2, location.length()-1);
				location = this.getLocalProperties().getProperty(location);
				if (location == null){
					String error = "unresolved placeholder: " + location;
					throw new IllegalArgumentException(error);
				}
			}
			resources[i++] = loader.getResource(location);
			persister.getZookeeperTemplate().subscribeDataChanges(location, this);
			layeredProperties.put(location, this.loadZkProperties(location));
		}
		super.setLocations(resources);
	}

	/**
	 * 依次加载所有locations的属性
	 */
	public Properties getProperties(){
		if (properties == null){
			try {
				properties = this.mergeProperties();
			} catch (IOException e) {
			}
		}
		return properties;
	}
	
	/**
	 * 本地文件的属性
	 */
	public Properties getLocalProperties(){
		return localProperties[0];
	}
	/**
	 * Zookeeper的属性
	 */
	public Properties getRemoteProperties(String location) {
		return layeredProperties.get(location);
	}
	
	/**
	 * 修改一个节点上的所有属性
	 */
	public void update(String location, Properties props) throws IOException{
		OutputStream os = loader.getResource(location).getOutputStream();
		persister.store(props, os, null);
		this.properties.putAll(props);
		os.close();
	}
	
	//==================== PropertyResourceConfigurer ================//
	private Map<String, List<String>> placeHolderMap = new HashMap<String, List<String>>();
	private ConfigurableListableBeanFactory beanFactory;
	@Override        //postProcessBeanFactory()
	protected void processProperties(ConfigurableListableBeanFactory beanFactory,  //DefaultListableBeanFactory
			 						Properties props) throws BeansException {
		this.beanFactory = beanFactory;
		//记录变量被哪些对象引用了
		BeanDefinition definition;
		for (String beanName : beanFactory.getBeanDefinitionNames()){
			definition = beanFactory.getBeanDefinition(beanName);
			for (PropertyValue pv : definition.getPropertyValues().getPropertyValueList()){
				if (pv.getValue() instanceof TypedStringValue){
					String var = ((TypedStringValue)pv.getValue()).getValue();
					if (var.startsWith("${") && var.endsWith("}")){
						var = var.substring(2, var.length()-1);
						String obj = beanName+"."+pv.getName();
						if (!placeHolderMap.containsKey(var)){
							placeHolderMap.put(var, new ArrayList<String>(2));
						}
						placeHolderMap.get(var).add(obj);
					}
				}
			}
		}
		
		super.processProperties(beanFactory, props);
	}
	
	//==================== ZooKeeperTemplate.ZkDataListener ================//
	@Override
	public void dataChanged(String dataPath, String data) {
		this.updateProperties((String)data);
		
		try{ //update Bean-Property
			String beanName, property, value;
			for (Map.Entry<String, List<String>> entry : placeHolderMap.entrySet()){
				value = properties.getProperty(entry.getKey());
				for (String beanProperty : entry.getValue()){
					int idx = beanProperty.lastIndexOf(".");
					beanName = beanProperty.substring(0, idx);
					property = beanProperty.substring(idx+1);
					BeanUtils.setPrivateField(beanFactory.getBean(beanName), property, value);
				}
			}
			//TODO 个别对象（如DataSource）需要重新build
//			beanFactory.destroyBean(beanName, beanFactory.getBean(beanName));
//			beanFactory.registerSingleton(beanName, singletonObject);
		}catch(Exception e){
			logger.error(e);
		}
	}
	
	private void updateProperties(String jsonProps){
		if (StringUtils.isEmpty(jsonProps)) {
			return;
		}
		Properties props = JSON.parseObject(jsonProps, Properties.class);
		if (this.localProperties != null) {
			for (Properties localProp : this.localProperties) {
				if (this.localOverride){ //localProp -> props
					CollectionUtils.mergePropertiesIntoMap(localProp, props);
				}else{  //props -> localProp
					Properties result = new Properties(localProp);
					result.putAll(props);
					props = result;
				}
			}
		}
		this.properties.putAll(props);
	}
	
	private ZooKeeperTemplate getZookeeperTemplate(String api, String servers,
						String authority, String rootPath) {
		AbstractZKTemplate template;
		if ("zkclient".equals(api)) {
			template = new ZkClientTemplate();
		} else {
			template = new CuratorTemplate();
		}
		template.setRootPath(rootPath)
				.setServers(servers)
				.setAuthority(authority);
		template.afterPropertiesSet();
		return template;
	}
	
	private Properties loadZkProperties(String location) {
		Properties props = new Properties();
		Resource resource = loader.getResource(location);
		try {
			persister.load(props, resource.getInputStream());
		} catch (IOException e) {
			logger.error("", e);
		}
		return props;
	}
}
