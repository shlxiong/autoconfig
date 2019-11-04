package com.openxsl.config.filter.tracing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.ttl.TransmittableThreadLocal;

import com.openxsl.config.filter.domain.InvocTrace;
import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.domain.InvokerInvocTrace;
import com.openxsl.config.filter.tracing.TraceContext.Tools;
import com.openxsl.config.logger.context.LoggerContext;

/**
 * 线程上下文
 * @author xiongsl
 */
public class TraceAsynContext {
	/**
	 * 不同于日志的SpanId，这里要体现上下级关系
	 */
	private static TransmittableThreadLocal<InvocTrace> rpcInf = new TransmittableThreadLocal<InvocTrace>();
	/**
	 * 统计子节点数
	 */
	private static TransmittableThreadLocal<Map<String,Integer>> familyMap
						= new TransmittableThreadLocal<Map<String,Integer>>();
	
	public static void initiate(String parentId) {
		familyMap.set(new ConcurrentHashMap<String,Integer>());
		InvocTrace invocTrace = new InvocTrace(parentId, getTraceId());
		rpcInf.set(invocTrace);
	}
	
	public static void transmitRpc(String parentId, String traceId) {
		rpcInf.set(new InvocTrace(parentId, traceId));
	}
	
	public static String newRpc(Invoker invoker, String... tracePId) {  //异步调用不会修改父线程的对象
		synchronized (rpcInf) {
			String parentId = innerRpc().getRpcId();
			Integer last = familyMap.get().get(parentId);
			if (last == null) {
				last = 1;
			} else {
				last ++;
			}
			familyMap.get().put(parentId, last);
			String rpcId = parentId+"."+Tools.toFixedSizeStr(last, 2);
			String traceId = tracePId.length>0 ? tracePId[0] : getTraceId();  //改变traceId
			
			rpcInf.set(new InvokerInvocTrace(rpcId, traceId, invoker));
			return rpcId;
		}
	}
	
	public static void clear() {
		familyMap.remove();
		rpcInf.remove();
	}
	
	public static String getTraceId(){
		if (rpcInf.get() != null) {
			return rpcInf.get().getTraceId();
		} else {
			return LoggerContext.getTraceId();
		}
    }
	
	public static InvocTrace innerRpc() {
		return rpcInf.get();
	}
	public static Map<String,Integer> getFamily(){
		return familyMap.get();
	}
	
}
