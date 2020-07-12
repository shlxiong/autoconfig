package com.openxsl.config.dal.zookeeper.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import com.openxsl.config.dal.zookeeper.ZooKeeperTemplate;
import com.openxsl.config.thread.GrouppedThreadFactory;

public abstract class AbstractZKTemplate implements ZooKeeperTemplate, InitializingBean {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	@Value("${zookeeper.namespace:/configsvr}")
	protected String rootPath;
	@Value("${zookeeper.address:127.0.0.1:2181}")
	private String servers;
	@Value("${zookeeper.authority:}")
	private String authority;
	
	private final List<ZkSessionListener> sessionListeners = 
				new ArrayList<ZkSessionListener>(2);
	
	private volatile boolean closed = false;
	private final String ID_NODE = "/GLOBALS/ID";
	private final ExecutorService executor = new ThreadPoolExecutor(2, 4,
				 		60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
				 		new GrouppedThreadFactory("ZkTemplate"));
	
	@Override
	public void afterPropertiesSet(){
		if (servers.startsWith("zookeeper://")){
			servers = servers.substring("zookeeper://".length());
		}
		servers = servers.replace("?backup=", ","); //dubbo集群方式 zookeeper://address1?backup=address2,address3
		int timeout = 300 * 1000;      //default timeout=Integer.MaxValue(2147483647)
		int sessionTime = 600 * 1000;  //default 30000
		logger.warn("wait for {}(ms) to connect Zookeeper: {}/{}", timeout,servers,rootPath);
		this.buildClient(servers, timeout, sessionTime, authority);
		
		sessionListeners.add(new InnerSessionListener());
		this.registerConnectState();
		
		((ThreadPoolExecutor)executor).allowCoreThreadTimeOut(true);
	}
	
	@Override
	public void subscribeStateChanges(ZkSessionListener listener) {
		sessionListeners.add(listener);
	}
	
	public void onStateChanged(int state) {
		for (ZkSessionListener listener : sessionListeners) {
			listener.stateChanged(state);
		}
	}
	
//	@Override
//	public abstract void subscribeDataChanges(String subPath, ZkDataListener listener);
	
	@Override
	public void close() {
		if (!closed) {
			try {
	            doClose();
	            closed = true;
	        } catch (Throwable t) {
	            logger.warn("", t);
	        }
        }
	}
	
	@Override
	public String getSequence(int length) throws Exception {
		String seqPath = this.createSequential(ID_NODE);
    	String digital = seqPath.toLowerCase().replaceAll("[^a-z|0-9]", "");
    	if (length > 0) {
    		digital = "0000000000" + digital;
    		digital = digital.substring(digital.length()-length);
    	}
    	//删掉
    	executor.execute(new Runnable() {
			@Override
			public void run() {
				delete(seqPath);
			}
		});
    	return digital;
	}
	
	@Override
	public ZooKeeperTemplate setRootPath(String path) {
		this.rootPath = (path==null||path.trim().length()==0) ? "/"
					: (path.charAt(0)=='/') ? path : '/'+path;
		return this;
	}
	@Override
	public ZooKeeperTemplate setServers(String servers) {
		this.servers = servers;
		return this;
	}
	@Override
	public ZooKeeperTemplate setAuthority(String authority) {
		this.authority = authority;
		return this;
	}
	
	protected abstract void buildClient(String servers, int connectTimeout, int socketTimeout,
					 String authority);
	
	protected abstract void registerConnectState();
	
	protected abstract void doClose();
	
	/**
	 * 当Zookeeper关闭或重启后，发送邮件
	 * @author xiongsl
	 */
	class InnerSessionListener implements ZkSessionListener {
		private long shutdownTime = -1;
		
		@Override
		public void stateChanged(int state) {
			if (state == 0) { //DISCONNECT: 断了
				shutdownTime = System.currentTimeMillis();
				logger.warn("Zookeeper lost connection, maybe shutdown");
				//TODO mail
			} else if (state == 1) { //CONNECTED: 正常或恢复了
				if (shutdownTime > 0) {
					long duration = System.currentTimeMillis() - shutdownTime;
					logger.warn("Zookeeper has beean shutdown: {}(ms)", duration);
					shutdownTime = -1;
					//TODO mail
				}
			} else if (state == 2) { //NEW_SESSION / EXPIRE
				logger.warn("session expired! To establish new connection.");
			}
		}
	}

}
