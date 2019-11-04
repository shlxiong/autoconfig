package com.openxsl.config.dal.zookeeper.lock;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.framework.recipes.locks.Lease;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.openxsl.config.thread.CoherentThreadPool;

/**
 * Curator实现的分布式锁。
 *    createsTheLock(): 在zookeeper中创建了EPHEMERAL_SEQUENTIAL节点，
 *    getsTheLock(): PredicateResults(haveLock, minPath)
 *    if (haveLock): return true;
 *    else: usingWatcher(watcher); wait_timeout
 *    deleteOurPath()
 *    
 * @author xiongsl
 */
public class DistributedLock {
	private final InterProcessLock lock;   //可重入锁，一个客户端可多次获得锁
	private InterProcessSemaphoreV2 semphore;  //最多同时允许N个线程访问临界资源。
	//InterProcessReadWriteLocK
	//InterProcessMultiLock
	private ThreadLocal<Lease> leaseCap = new ThreadLocal<Lease>();
	
	public DistributedLock(CuratorFramework client, String name, boolean reentrant) {
		String lockPath = "/GLOBAL/LOCKS/" + name;
		lock = reentrant ? new InterProcessMutex(client, lockPath)
				: new InterProcessSemaphoreMutex(client, lockPath);  //InterProcessSemaphoreV2 on maxLeases=1
		semphore = null;
	}
	public DistributedLock(CuratorFramework client, String lockPath, int concurrents) {
		semphore = new InterProcessSemaphoreV2(client, lockPath, concurrents);
		lock = null;
	}
	
	public boolean lock(long timeout, TimeUnit unit) {
		try {
			if (lock != null) {
				return lock.acquire(timeout, unit);
			} else {
				Lease lease = semphore.acquire(timeout, unit);
				if (lease != null) {
					leaseCap.set(lease);
					return true;
				}
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void unlock() {
		try {
			if (lock != null) {
				lock.release();
			} else {
				semphore.returnLease(leaseCap.get());
				leaseCap.remove();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("localhost:2181")
				.retryPolicy(new ExponentialBackoffRetry(1000, 3, 3000))
				.connectionTimeoutMs(5000)
				.sessionTimeoutMs(5000)
				.namespace("configsvr")
				.build();
		client.start();
		new CoherentThreadPool(5).execute(new Runnable() {
			@Override
			public void run() {
				new DistributedLock(client, "MY", true).
					lock(5, TimeUnit.SECONDS);
			}
		});
	}

}
