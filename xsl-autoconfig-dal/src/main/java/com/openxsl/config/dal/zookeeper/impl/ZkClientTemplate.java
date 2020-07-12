package com.openxsl.config.dal.zookeeper.impl;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.context.annotation.Scope;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.condition.ConditionalProperty;
import com.openxsl.config.dal.zookeeper.ZkNodeData;
import com.openxsl.config.util.StringUtils;

@Scope("prototype")
@ScanConfig
@ConditionalProperty(name="zookeeper.client.api", havingValue="zkclient", matchIfMissing=true)
public class ZkClientTemplate extends AbstractZKTemplate {
	private final List<ACL> acls = new ArrayList<ACL>(2);
	private ZkClient client;
	
	@Override
	public void create(String subPath, Object value, boolean temporary){
		String path = this.normalizePath(subPath);
		if (!client.exists(path)){
			this.createParents(path);
			if (temporary){   //Ephemeral的父节点不能是Ephemeral
				if (acls.size() > 0) {
					client.createEphemeral(path, acls);
				} else {
					client.createEphemeral(path);
				}
			}else{
				if (acls.size() > 0) {
					client.createPersistent(path, true, acls);
				} else {
					client.createPersistent(path, true);
				}
			}
		}
		
		if (value != null){
			client.writeData(path, value);
		}
	}
	
	@Override
	public void delete(String subPath){
		try {
			client.deleteRecursive(this.normalizePath(subPath));
		} catch (ZkNoNodeException e) {
		}
	}
	
	@Override
	public <T> T get(String subPath, Class<T> clazz) {
		try{
			String jsonStr = client.readData(this.normalizePath(subPath));
			return JSON.parseObject(jsonStr, clazz);
		}catch(ZkNoNodeException ne){
			return null;
		}
	}
	
	@Override
	public void save(String subPath, Object value){
		client.writeData(this.normalizePath(subPath), value);
	}
	
	/**
	 * 列举子节点
	 */
	@Override
	public List<String> getChildren(String subPath) {
        try {
            return client.getChildren(this.normalizePath(subPath));
        } catch (ZkNoNodeException e) {
            return new ArrayList<String>(0);
        }
    }
	
	/**
	 * 判断是否树节点存在
	 */
	@Override
	public boolean exists(String subPath) {
        try {
            return client.exists(this.normalizePath(subPath));
        } catch (Throwable t) {
        	return false;
        }
    }
	
	@Override
	public int countChildren(String subPath) {
		try {
			return client.countChildren(subPath);
		} catch (Throwable t) {
        	return 0;
        }
	}
	
	@Override
	public ZkNodeData getWithStat(String subPath) {
		try{
			Stat stat = new Stat();
			String jsonStr = client.readData(this.normalizePath(subPath), stat);
			ZkNodeData zknode = new ZkNodeData(stat);
			zknode.setData(jsonStr);
			return zknode;
		}catch(ZkNoNodeException ne){
			return null;
		}
	}
	
	@Override
	public void subscribeDataChanges(String subPath, final ZkDataListener listener) {
		String dataPath = this.normalizePath(subPath);
		client.subscribeDataChanges(dataPath, new IZkDataListener() {
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				this.handleDataChange(dataPath, null);
			}
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				logger.info("Zookeeper [{}] data has been changed: {}", dataPath, data);
				listener.dataChanged(dataPath, (String)data);
			}
		});
	}
	
	@Override
	public void subscribeChildChanges(String subPath, ZkChildListener listener) {
		String dataPath = this.normalizePath(subPath);
		client.subscribeChildChanges(dataPath, new IZkChildListener() {
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				logger.info("Zookeeper [{}] children has been changed: size={}", subPath,currentChilds.size());
				listener.childChanged(parentPath, currentChilds);
			}
		});
	}
	
	@Override
	public String createSequential(String subPath) {
		String path = this.normalizePath(subPath);
		this.createParents(path);
		return client.create(path, null, CreateMode.PERSISTENT_SEQUENTIAL);
	}
	
	@Override
	protected void buildClient(String servers, int connectTimeout, int socketTimeout, String authority) {
		client = new ZkClient(servers, socketTimeout, connectTimeout);
		if (!StringUtils.isEmpty(authority)) {
			final String scheme = "digest";
			client.addAuthInfo(scheme, authority.getBytes());
	    	try {
				acls.add(new ACL(Perms.ALL, new Id(scheme, DigestAuthenticationProvider.generateDigest(authority))));
			} catch (NoSuchAlgorithmException e) {
				logger.error("NoSuchAlgorithmException: SHA1");
			}
		}
		client.setZkSerializer(new ZkSerializer() {
			@Override
			public byte[] serialize(Object data) throws ZkMarshallingError {
				return JSON.toJSONString(data).getBytes();
			}
			@Override
			public Object deserialize(byte[] bytes) throws ZkMarshallingError {
				return JSON.parseObject(new String(bytes), String.class);
			}
		});
		
		this.create("/", "", false);  //client.createPersistent(this.rootPath, true);
	}

	@Override
	protected void registerConnectState() {
		client.subscribeStateChanges(new IZkStateListener() {
            @Override
			public void handleStateChanged(KeeperState state) throws Exception {
                if (state == KeeperState.Disconnected) {
                	ZkClientTemplate.this.onStateChanged(0);
                } else if (state == KeeperState.SyncConnected) {  //recover
                	ZkClientTemplate.this.onStateChanged(1);
                }
            }

            @Override
			public void handleNewSession() throws Exception {  //KeeperState.Expired
            	ZkClientTemplate.this.onStateChanged(2);
            }

			@Override
			public void handleSessionEstablishmentError(Throwable arg0) throws Exception { //AuthFailed
				logger.error("establish connection failed, maybe AuthFailed!");
			}
        });
	}
	
	@Override
	protected void doClose() {
		client.close();
	}
	
	private final String normalizePath(String subPath){
		if (subPath.charAt(0) != '/'){
			subPath = '/' + subPath;
		}else{
			while (subPath.length() > 1 && subPath.charAt(1) == '/'){
				subPath = subPath.substring(1);
			}
		}
		while (subPath.endsWith("/")){
			subPath = subPath.substring(0, subPath.length()-1);
		}
		if (subPath.length() < 1) {
			return rootPath;
		}
		return "/".equals(rootPath) ? subPath : (rootPath + subPath);
	}
	
	private void createParents(String path) {
		if (path.length() <= rootPath.length()) {
			return;
		}
		String parent = path.substring(0, path.lastIndexOf('/')+1);
		int idx = parent.indexOf("/", rootPath.length()+1);
		while (idx > 0) {
			String node = parent.substring(0, idx);
			if (!exists(node)) {
				if (acls.size() > 0) {
					client.createPersistent(node, true, acls);
				} else {
					client.createPersistent(node, true);
				}
			}
			idx = parent.indexOf("/", node.length()+1);
		}
	}
	

}
