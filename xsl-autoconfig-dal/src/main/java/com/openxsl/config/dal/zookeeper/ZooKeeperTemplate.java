package com.openxsl.config.dal.zookeeper;

import java.util.List;

/**
 * Zookeeper操作类，这里所有的路径都是相对路径
 * @author xiongsl
 */
public interface ZooKeeperTemplate {
	
	/**
	 * 创建或修改树节点（如果不存在），写入数据（value!=null)
	 * @param subPath 节点路径
	 * @param value   数据
	 * @param temporary EPHERMERAL
	 */
	public void create(String subPath, Object value, boolean temporary);
	
	/**
	 * 删除节点，包括子节点
	 * @param subPath
	 */
	public void delete(String subPath);
	
	/**
	 * 读取节点数据
	 */
	public <T> T get(String subPath, Class<T> clazz);
	
	/**
	 * 只保存数据，如果节点不存在，将会抛错
	 * @see #create(String, Object)
	 * @param subPath
	 * @param value
	 */
	public void save(String subPath, Object value);
	
	public List<String> getChildren(String subPath);
	
	public boolean exists(String subPath);
	
	public int countChildren(String subPath);
	
	public ZkNodeData getWithStat(String subPath);
	
	/**
	 * 创建顺序增长的子节点
	 * @param subPath
	 */
	public String createSequential(String subPath);
	
	/**
	 * 生成递增的全局唯一性ID
	 * @param length 长度
	 * @throws Exception
	 */
	public String getSequence(int length) throws Exception;
	
	/**
	 * 监听数据
	 */
	public void subscribeDataChanges(String subPath, ZkDataListener listener);
	
	/**
	 * 监听子节点变化(增删改)
	 */
	public void subscribeChildChanges(String subPath, ZkChildListener listener);
	
	/**
	 * 监听连接状态
	 */
	public void subscribeStateChanges(ZkSessionListener listener);
	
	public void close();
	
	public ZooKeeperTemplate setServers(String servers);
	public ZooKeeperTemplate setAuthority(String authority);
	public ZooKeeperTemplate setRootPath(String rootPath);
	
//	public void getTree(String root){
//		//TODO
//	}
	
	interface ZkSessionListener{
		
		public void stateChanged(int state);
		
	}
	
	interface ZkDataListener{
		/**
		 * 当data=null时，数据被删除
		 */
		public void dataChanged(String subPath, String data);
		
	}
	
	interface ZkChildListener{

		public void childChanged(String subPath, List<String> children);
		
	}
	
}
