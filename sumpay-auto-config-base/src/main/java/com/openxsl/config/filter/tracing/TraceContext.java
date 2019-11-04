package com.openxsl.config.filter.tracing;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.Environment;
import com.openxsl.config.filter.domain.InvocTrace;
import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.domain.InvokerInvocTrace;
import com.openxsl.config.logger.context.LoggerContext;

/**
 * 上下文对象
 * @author xiongsl
 */
public class TraceContext {
	private static final Logger LOG = LoggerFactory.getLogger(TraceContext.class);
	private static final String ROOT_ID = "0";
	private static boolean disabled = !Environment.getProperty("spring.tracing.enable", 
											Boolean.class, true);
	
	/**
	 * 同步调用ThreadLocal容易被被擦掉，使用Stack保留痕迹
	 */
	private static ThreadLocal<Stack<InvocTrace>> rpcStack
						= new ThreadLocal<Stack<InvocTrace>>();
	/**
	 * 统计子节点数
	 */
	private static ThreadLocal<Map<String,Integer>> familyMap
						= new ThreadLocal<Map<String,Integer>>();
	
	
//	/**
//	 * 单个线程循环处理
//	 */
//	private static TransmittableThreadLocal<Snapshot> snapshot
//						= new TransmittableThreadLocal<Snapshot>();
	
	public static void initiate(String rpcId, Invoker invoker, String... tracePId) {
		if (disabled) {
			return;
		}
		if (rpcId == null) {
			rpcId = ROOT_ID;
		}
		String traceId;
		if (tracePId.length > 0) {  //改变traceId
			traceId = tracePId[0];
			LoggerContext.setTraceId(traceId);
		} else {
			traceId = getTraceId();
		}
//		snapshot.set(new Snapshot(traceId, rpcId));
		if (invoker == null) {
			invoker = new Invoker("BOOT", "annoymous or root-thread", "start");
		}
		invoker.setApplication(Environment.getApplication());
		invoker.setOwner(Environment.getOwner());
		invoker.setHost(Environment.getAddress().split(":")[0]);
		
		rpcStack.set(new Stack<InvocTrace>());
		familyMap.set(new ConcurrentHashMap<String,Integer>());
		rpcStack.get().push(new InvokerInvocTrace(rpcId, traceId, invoker));
	}
	public static void clear() {
		familyMap.remove();
		rpcStack.remove();
//		snapshot.remove();
	}
	
	public static String newRpc(Invoker invoker) {
		if (disabled) {
			return null;
		}
		synchronized (rpcStack) {
			String parentId = getRpcId();
			if (parentId == null) {
				return null;
			}
			Integer last = familyMap.get().get(parentId);
			if (last == null) {
				last = 1;
			} else {
				last ++;
			}
			familyMap.get().put(parentId, last);
			String rpcId = parentId+"."+Tools.toFixedSizeStr(last, 2);
			String traceId = getTraceId();
			
			rpcStack.get().push(new InvokerInvocTrace(rpcId, traceId, invoker));
			return rpcId;
		}
	}

	public static InvocTrace popStack() {
		if (disabled) {
			return null;
		}
		try {
			return rpcStack.get().pop();
		} catch (NullPointerException npe) {
			return null;
		}
	}
	
	public static boolean isOver() {
		if (disabled) {
			return true;
		}
		try {
			return rpcStack.get().isEmpty();
		} catch (NullPointerException npe) {
			return true;
		}
	}
	public static boolean isDisabled() {
		return disabled;
	}
	
	public static class Tools{
		public static String toFixedSizeStr(int idx, int length){
			StringBuilder buffer = new StringBuilder();
			for (int i=0; i<length; i++){
				buffer.append("0");
			}
			buffer.append(idx);
			return buffer.substring(buffer.length()-length);
		}
	}
	
	public static String getTraceId(){
		return LoggerContext.getTraceId();
    }
	public static String getRpcId() {
		if (disabled) {
			return null;
		}
		try {
			return innerRpc().getRpcId();
		} catch (NullPointerException npe) {
			LOG.warn("Has no InvocTrace in ThreadContext, Please set initiate()");
			return null;
		}
	}
	public static Invoker getInvoker() {
		if (disabled) {
			return null;
		}
		try {
			return ((InvokerInvocTrace)innerRpc()).getInvoker();
		} catch (NullPointerException npe) {
			LOG.warn("Has no InvocTrace in ThreadContext, Please set initiate()");
			return null;
		}
	}
	
	public static void dump() {
		StringBuilder buffer = new StringBuilder();
		if (disabled) {
			buffer.append("【dump】: tracing is disabled");
		} else {
			if (isOver()) {
				buffer.append("【InvocTrace】: No trace");
			} else {
				buffer.append("【InvocTrace】: ")
					  .append(((InvokerInvocTrace)innerRpc()).getInvoker());
				for (InvocTrace trace : rpcStack.get()) {
					buffer.append("\n\t").append(trace.getTraceId()).append(":")
						.append(trace.getRpcId());
				}
			}
			if (familyMap.get() != null) {
				buffer.append("\n【family】: ").append(familyMap.get());
			}
		}
		LOG.info(buffer.toString());
	}
	
	static InvocTrace innerRpc() {
		if (disabled) {
			return null;
		}
		return rpcStack.get().peek();
	}
	
	static class Snapshot{
		private String traceId;
		private String rpcId;
		
		public Snapshot(String traceId, String rpcId) {
			this.setTraceId(traceId);
			this.setRpcId(rpcId);
		}
		public String getTraceId() {
			return traceId;
		}
		public void setTraceId(String traceId) {
			this.traceId = traceId;
		}
		public String getRpcId() {
			return rpcId;
		}
		public void setRpcId(String rpcId) {
			this.rpcId = rpcId;
		}
	}
}
