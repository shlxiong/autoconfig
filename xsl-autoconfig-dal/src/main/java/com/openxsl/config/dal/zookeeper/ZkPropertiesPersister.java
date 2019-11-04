package com.openxsl.config.dal.zookeeper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.springframework.util.DefaultPropertiesPersister;

import com.openxsl.config.dal.zookeeper.ZkResourceLoader.ZkNodeInpustream;
import com.openxsl.config.dal.zookeeper.ZkResourceLoader.ZkNodeOuputstream;

/**
 * 存、取Properties对象到ZooKeeper
 * @author 001327
 */
public class ZkPropertiesPersister extends DefaultPropertiesPersister {
	private ZooKeeperTemplate template;
	public void setZookeeperTemplate(ZooKeeperTemplate template){
		this.template = template;
	}
	public ZooKeeperTemplate getZookeeperTemplate(){
		return template;
	}

	@Override
	public void load(Properties props, InputStream is) throws IOException {
		String subPath = ((ZkNodeInpustream)is).getNode();
		try{
			props.putAll(template.get(subPath, Properties.class));
		}catch(NullPointerException npe){
			//ignoreResourceNotFound
		}
	}

	@Override
	public void store(Properties props, OutputStream os, String header)
			throws IOException {
		if (props==null || props.size()<1) {
			return;
		}
		String subPath = ((ZkNodeOuputstream)os).getNode();
		template.create(subPath, props, false);
	}

}
