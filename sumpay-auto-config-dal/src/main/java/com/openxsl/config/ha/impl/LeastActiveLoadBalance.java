package com.openxsl.config.ha.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.openxsl.config.ha.Cluster;

public class LeastActiveLoadBalance<T> extends AbstractLoadBalance<T> {
	private Cluster cluster;
	
	public LeastActiveLoadBalance(String cluster){
		this.cluster = Cluster.valueOf(cluster.toUpperCase());
	}

	@Override
	protected int doSelect() {
		int idx = 0;
		while (idx < size()){
			if (!invokers.get(idx).isDisabled()){
				return idx;
			}
			idx++;
		}
		throw new IllegalStateException("all invokers are disabled");
	}
	
	@Override
	public Cluster getCluster() {
		return cluster;
	}
	
	@Override
	public void resort(){
		//按lastAccess从小到大
		Collections.sort(invokers, new Comparator<InvokerWrap<T>>(){
			@Override
			public int compare(InvokerWrap<T> invoker1, InvokerWrap<T> invoker2) {
				long mils = invoker1.getLastAccess() - invoker2.getLastAccess();
				return (int)mils;
			}
		});
	}
	
	public static void main(String[] args) {
		LeastActiveLoadBalance<String> balance = new LeastActiveLoadBalance<String>("failover");
		List<String> invokers = new ArrayList<String>();
		invokers.add("a.htm");
		invokers.add("b.htm");
		balance.setInvokers(invokers);
		System.out.println(balance.select());
		System.out.println(balance.select());
		System.out.println(balance.select());
	}

}
