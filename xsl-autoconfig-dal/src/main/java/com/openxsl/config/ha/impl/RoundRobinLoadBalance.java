package com.openxsl.config.ha.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.openxsl.config.ha.Cluster;

public class RoundRobinLoadBalance<T> extends AbstractLoadBalance<T> {
	private AtomicInteger current = new AtomicInteger(0);
	private Cluster cluster;
	
	public RoundRobinLoadBalance(String cluster){
		this.cluster = Cluster.valueOf(cluster.toUpperCase());
	}

	@Override
	protected int doSelect() {
		int idx = current.getAndIncrement();
		if (idx >= this.size()){
			current.set(0);
			return 0;
		}else{
			return idx;
		}
	}
	
	@Override
	public Cluster getCluster() {
		return cluster;
	}
	
}
