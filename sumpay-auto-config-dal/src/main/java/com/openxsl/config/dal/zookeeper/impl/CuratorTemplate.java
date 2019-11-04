package com.openxsl.config.dal.zookeeper.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.annotation.Scope;

import com.alibaba.fastjson.JSON;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.condition.ConditionalProperty;
import com.openxsl.config.dal.zookeeper.LeaderExecuteAction;
import com.openxsl.config.dal.zookeeper.TransactionExecuteAction;
import com.openxsl.config.dal.zookeeper.ZkNodeData;
import com.openxsl.config.exception.SystemException;
import com.openxsl.config.util.StringUtils;

@Scope("prototype")
@ScanConfig
@ConditionalProperty(name="zookeeper.client.api", havingValue="curator")
public class CuratorTemplate extends AbstractZKTemplate {
	private CuratorFramework client;
	private final ConcurrentMap<String, NodeCache> dataCacheMap
					= new ConcurrentHashMap<String, NodeCache>();
	private final ConcurrentMap<String, PathChildrenCache> childCacheMap
					= new ConcurrentHashMap<String, PathChildrenCache>();
	
	public void switchRootPath(String rootPath) {
		client.usingNamespace(rootPath);
	}
	
	@Override
	public void create(String subPath, Object value, boolean temporary) {
		if (!this.exists(subPath)) {
			CreateMode mode = temporary ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT;
			try {
				client.create().creatingParentsIfNeeded().withMode(mode).forPath(subPath);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		if (value != null) {
			this.save(subPath, value);
		}
	}

	@Override
	public void delete(String subPath) {
		try {
			client.delete().deletingChildrenIfNeeded().forPath(subPath);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public <T> T get(String subPath, Class<T> clazz) {
		try {
			byte[] data = client.getData().forPath(subPath);
			return JSON.parseObject(new String(data), clazz);
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	@Override
	public void save(String subPath, Object value) {
		try {
			client.setData().forPath(subPath, JSON.toJSONString(value).getBytes());
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public List<String> getChildren(String subPath) {
		try {
			return client.getChildren().forPath(subPath);
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	@Override
	public boolean exists(String subPath) {
		try {
			return client.checkExists().forPath(subPath) != null;
		} catch (Exception e) {
			logger.error("", e);
			return false;
		}
	}
	
	@Override
	public int countChildren(String subPath) {
		try {
            Stat stat = client.checkExists().forPath(subPath);
            if (null != stat) {
                return stat.getNumChildren();
            }
        } catch (Exception ex) {
        	logger.error("", ex);
        }
        return 0;
	}
	
	@Override
	public ZkNodeData getWithStat(String subPath) {
		try {
			NodeCache nodeCache = new NodeCache(client, subPath);
			nodeCache.start(true); //这个参数要给true  不然下边空指针...
			byte[] data = nodeCache.getCurrentData().getData();
			Stat stat = nodeCache.getCurrentData().getStat();
			ZkNodeData zknode = new ZkNodeData(stat);
			zknode.setData(data==null ? "" : new String(data));
			nodeCache.close();
			return zknode;
		} catch(Exception e){
			logger.error("", e);
		}
		return null;
	}
	
	@Override
	public void subscribeDataChanges(String subPath, final ZkDataListener listener) {
		try {
			final NodeCache cache = this.getNodeCache(subPath);
			cache.getListenable().addListener( new NodeCacheListener() {
				@Override
				public void nodeChanged() throws Exception {
					String data = null;
					try {
						data = new String(cache.getCurrentData().getData());
					} catch (NullPointerException npe) { //delete
					}
					logger.info("Zookeeper [{}] data has been changed: {}", subPath, data);
					listener.dataChanged(subPath, data);
				}
			});
			cache.start();   //能否重复？
		}catch(Exception e) {
			logger.error("", e);
		}
	}
	private NodeCache getNodeCache(String subPath) {
		if (!dataCacheMap.containsKey(subPath)) {
			dataCacheMap.put(subPath, new NodeCache(client, subPath));
		}
		return dataCacheMap.get(subPath);
	}
	
	@Override
	public void subscribeChildChanges(String subPath, final ZkChildListener listener) {
		try {
			final PathChildrenCache cache = this.getChildCache(subPath);
			cache.getListenable().addListener(new PathChildrenCacheListener() {
				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					//event.getData():变化的节点 ;    event.getType():事件类型
					List<String> children = new ArrayList<String>();
					for (ChildData childData : cache.getCurrentData()) {
						children.add(childData.getPath());
					}
					logger.info("Zookeeper [{}] children has been changed: {}", subPath, event.getType());
					listener.childChanged(subPath, children);
				}
			});
			cache.start();
		}catch(Exception e) {
			logger.error("", e);
		}
	}
	private PathChildrenCache getChildCache(String subPath) {
		if (!childCacheMap.containsKey(subPath)) {
			childCacheMap.put(subPath, new PathChildrenCache(client, subPath, true));
		}
		return childCacheMap.get(subPath);
	}
	
	/**
	 * 在事务中执行操作.
	 * @param action
	 */
	public void executeInTransaction(TransactionExecuteAction action) {
		try {
            CuratorTransactionFinal curatorTransactionFinal = client.inTransaction().check().forPath("/").and();
            action.execute(curatorTransactionFinal);
            Collection<CuratorTransactionResult> results = curatorTransactionFinal.commit();
            for (CuratorTransactionResult result : results) {  
                System.out.println(result.getForPath() + " - " + result.getType());  
            }
        } catch (final Exception ex) {
        	this.handleException(ex);
        }
	}
	/**
     * 在主节点执行操作.
     * @param latchNode 分布式锁节点名称
     * @param callback  执行操作的回调
     */
    public void executeInLeader(final String latchNode, final LeaderExecuteAction action) {
        try (LeaderLatch latch = new LeaderLatch(client, latchNode)) {
            latch.start();
            latch.await();
            action.execute();
        } catch (final Exception ex) {
            this.handleException(ex);
        }
    }
    private void handleException(final Exception ex) {
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new SystemException(ex);
        }
    }
    
    @Override
	public String createSequential(String subPath) {
		try {
			return client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(subPath);
		} catch (Exception e) {
			logger.warn("", e);
			return null;
		}
	}

	@Override
	protected void buildClient(String servers, int connectTimeout, int socketTimeout, String authority) {
		try {
			String namespace = (rootPath.charAt(0)=='/') ? rootPath.substring(1) : rootPath;
			CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
					.connectString(servers)
					.retryPolicy(new ExponentialBackoffRetry(1000, 3, 3000))  //new RetryNTimes(1, 1000)   Math.min(base*n, max)
					.connectionTimeoutMs(connectTimeout)
					.sessionTimeoutMs(socketTimeout)
					.namespace(namespace);
			if (!StringUtils.isEmpty(authority)) {  //dubbo:openxsl
                builder = builder.authorization("digest", authority.getBytes());
            }
			client = builder.build();
			client.start();
			
			if (!client.blockUntilConnected(2*connectTimeout, TimeUnit.MILLISECONDS)) {
				client.close();
				throw new KeeperException.OperationTimeoutException();
			}
		} catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
	}

	@Override
	protected void registerConnectState() {
		client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                if (state == ConnectionState.LOST) {
                	CuratorTemplate.this.onStateChanged(0);
                } else if (state == ConnectionState.CONNECTED) {
                	CuratorTemplate.this.onStateChanged(1);
                } else if (state == ConnectionState.RECONNECTED) {
                	CuratorTemplate.this.onStateChanged(2);
                }
            }
        });
	}

	@Override
	protected void doClose() {
		for (Entry<String, NodeCache> each : dataCacheMap.entrySet()) {
			CloseableUtils.closeQuietly(each.getValue());
        }
		for (Entry<String, PathChildrenCache> each : childCacheMap.entrySet()) {
			CloseableUtils.closeQuietly(each.getValue());
        }
		try {
            Thread.sleep(500L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        CloseableUtils.closeQuietly(client);
	}

}
