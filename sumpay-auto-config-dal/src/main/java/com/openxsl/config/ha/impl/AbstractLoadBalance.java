package com.openxsl.config.ha.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.util.Assert;

import com.openxsl.config.ha.LoadBalance;

public abstract class AbstractLoadBalance<T> implements LoadBalance<T> {
	protected List<InvokerWrap<T>> invokers;
	private int size;
	
	public static <T> LoadBalance<T> getLoadBalance(String strategy, String cluster){
		if ("leastActive".equals(strategy)){
			return new LeastActiveLoadBalance<T>(cluster);
		}else{
			return new RoundRobinLoadBalance<T>(cluster);
		}
	}
	@Override
	public void setInvokers(List<T> invokers){
		Assert.notEmpty(invokers, "invokers must not be empty(at least 1 element)");
		
		this.size = invokers.size();
		this.invokers = Collections.synchronizedList(
							new ArrayList<InvokerWrap<T>>(size));
		for (T invoker : invokers){
			this.invokers.add(new InvokerWrap<T>(invoker));
		}
		//每个一分钟调整排序一次
		new Timer("loadbalance").schedule(new TimerTask(){
			@Override
			public void run() {
				resort();
				reset();
			}}, 60000, 60000);
	}
	
	protected abstract int doSelect();
	
	@Override
	public T select() {
		for (int i=0; i<size; i++){
			int idx = doSelect();
			if (!invokers.get(idx).isDisabled()){
				invokers.get(idx).access();
				return invokers.get(idx).getInvoker();
			}
		}
		
		throw new IllegalStateException("all invokers are disabled");
	}
	
	@Override
	public List<T> getAll(){
		List<T> list = new ArrayList<T>(size);
		for (InvokerWrap<T> elt : invokers){
			if (!elt.isDisabled()){
				list.add(elt.getInvoker());
			}
		}
		return list;
	}
	
	@Override
	public void remove(T invoker){
		for (InvokerWrap<T> elt : invokers){
			if (invoker.equals(elt.getInvoker())){
				elt.setDisabled(true);
				break;
			}
		}
	}
	
	@Override
	public int size(){
		return this.size;
	}
	
	public void setWeight(T invoker, float tps){
		InvokerWrap<T> target = null;
		for (InvokerWrap<T> elt : invokers){
			if (invoker.equals(elt.getInvoker())){
				target = elt;
				break;
			}
		}
		Assert.notNull(target, "'invoker' not exists");
		target.put(tps);
	}
	
	public void resort(){
		//按tps从大到小排序
		Collections.sort(invokers, new Comparator<InvokerWrap<T>>(){
			@Override
			public int compare(InvokerWrap<T> invoker1, InvokerWrap<T> invoker2) {
				int r = invoker2.getAvgTps() - invoker1.getAvgTps();
				if (r == 0){
					r = invoker1.getSuccs() - invoker2.getSuccs();
				}
				return r;
			}
		});
	}
	private final void reset(){
		for (InvokerWrap<T> elt : invokers){
			elt.reset();
		}
	}
}
