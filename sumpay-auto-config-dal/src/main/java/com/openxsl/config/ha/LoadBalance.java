package com.openxsl.config.ha;

import java.util.List;

/**
 * 负载均衡策略
 * 
 * RoundRobin：轮询
 * Random：随机
 * LeastActive：最近最少使用
 * ConcurrentHash：一致性哈希，按某些参数的哈希值
 * 
 * @author xiongsl
 */
public interface LoadBalance<T> {
	
	/**
	 * 选择一个调用者
	 */
	public T select();
	
	public int size();
	
	public void remove(T invoker);
	
	public void setInvokers(List<T> invokers);
	
	public List<T> getAll();
	
	public Cluster getCluster();
	
	public class InvokerWrap<T>{
		private final int MAX = 10000000;
		
		private T invoker;
		private boolean disabled = false;
		private int fails;
		private int total;
		private float sumTps = MAX;   //初始化较大值，优先级更高
		private long lastAccess = Long.MAX_VALUE;  //初始值较大，不容易被选中
		
		public InvokerWrap(T invoker){
			this.invoker = invoker;
		}
		@Override
		public String toString(){
			return invoker.toString()
					+"/"+lastAccess;
		}
		
		public int getAvgTps(){
			//rate = (total-fails)/total;
			//absTps = sumTps / total;
			//result = absTps * rate
			//-->result = 1.0f * sumTps*(total-fails) / total^2
			float temp = sumTps * (total-fails);
			int total2 = total * total;
			return (int)(temp / total2);
		}
		
		public void access(){
			lastAccess = System.currentTimeMillis();
		}
		
		public void put(float tps){
			if (tps > 0){
				if (total == 0){
					sumTps = tps;
				}else{
					sumTps += tps;
				}
			}else{
				fails++;
			}
			total++;
		}
		
		public void reset(){
			if (total >= 1000){
				fails = 0;
				total = 0;
				sumTps = MAX;
				disabled = false;
				lastAccess = Long.MAX_VALUE;
			}
		}
		
		public long getLastAccess(){
			return lastAccess;
		}
		public T getInvoker(){
			return invoker;
		}
		public boolean isDisabled(){
			return disabled;
		}
		public void setDisabled(boolean flag){
			this.disabled = flag;
		}
		public int getSuccs(){
			return total - fails;
		}
	}

}
