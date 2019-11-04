package com.openxsl.config.dal.zookeeper.lock;

import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.atomic.PromotedToLock;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * curator基于zookeeper实现的分布式数字计算
 * DistributedAtomicLong采用乐观锁与互斥锁结合方式
 * 
 * @author xiongsl
 */
public class DistributedCounter {
	private final DistributedAtomicLong distLong;
	private final String basePath = "/GLOBAL/COUNTER/$";
	
	public DistributedCounter(CuratorFramework client, String name, long value) {
		String counterPath = basePath.replace("$", name);
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 2);
		PromotedToLock lock = PromotedToLock.builder()
				.lockPath(counterPath+"/_LOCK").timeout(5, TimeUnit.SECONDS)
				.retryPolicy(retryPolicy)
				.build();
		distLong = new DistributedAtomicLong(client, counterPath, retryPolicy, lock);
		try {
			distLong.initialize(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public long get() throws Exception {
		return distLong.get().postValue();
	}
	
	/**
	 * trySet()提现了乐观锁和互斥锁的实现，下面的add,increase等皆调用了该方法
	 */
	public long set(long num) throws Exception {
		try {
			return distLong.trySet(num).postValue();
		} catch (Exception e) {
			return this.get();
		}
	}
	
	public long add(long num) throws Exception {
		try {
			return distLong.add(num).postValue();
		} catch (Exception e) {
			return this.get();
		}
	}
	public long substract(long num) throws Exception {
		try {
			return distLong.subtract(num).postValue();
		} catch (Exception e) {
			return this.get();
		}
	}
	
	public long increase() throws Exception {
		try {
			return this.add(1);
		} catch (Exception e) {
			return this.get();
		}
	}
	public long decrease() throws Exception {
		try {
			return this.add(-1);
		} catch (Exception e) {
			return this.get();
		}
	}

}
