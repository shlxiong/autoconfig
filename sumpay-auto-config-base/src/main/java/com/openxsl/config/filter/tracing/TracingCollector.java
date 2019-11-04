package com.openxsl.config.filter.tracing;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.filter.domain.InvocTrace;
import com.openxsl.config.filter.domain.Invoker;
import com.openxsl.config.filter.domain.InvokerInvocTrace;
import com.openxsl.config.loader.GraceServiceLoader;
import com.openxsl.config.util.NetworkUtils;

/**
 * 调用轨迹收集器
 * @author xiongsl
 */
public class TracingCollector {
	private static final Logger logger = LoggerFactory.getLogger("TraceContext");

	static {
		Iterator<TracingSender> itr = //ServiceLoader.load(TracingSender.class).iterator();
						GraceServiceLoader.loadServices(TracingSender.class).iterator();
		if (itr.hasNext()) {
			setSender(itr.next());
		}
	}

	/**
	 * 初始化调用链的上下文
	 * @param rpcId 父ID
	 */
	public static void start(String rpcId) {
		TraceContext.initiate(rpcId, null);
	}
	public static void setSender(TracingSender sender) {
		logger.info("set TracingSender: {}", sender.getClass().getName());
		TracingCollector.sender = sender;
	}
	
	public static void setT1(Invoker invoker) {
		if (TraceContext.isDisabled()) {
			return;
		}
		String rpcId = (invoker == null) ? TraceContext.getRpcId()
					: TraceContext.newRpc(invoker);
		if (rpcId == null) {
			return;
		}
		TraceContext.innerRpc().setT1(System.currentTimeMillis());
		TraceContext.innerRpc().setT2(0);
		TraceContext.innerRpc().setT3(0);
		TraceContext.innerRpc().setT4(0);
	}
	public static void setT2(Long... timestamp) {
		if (TraceContext.isDisabled()) {
			return;
		}
		long t2 = (timestamp.length>0) ? timestamp[0] : System.currentTimeMillis();
		TraceContext.innerRpc().setT2(t2);
		send();
	}
	/**
	 * 客户端标识Server接收到请求（SR）
	 */
	public static void setT3(Long... timestamp) {
		if (TraceContext.isDisabled()) {
			return;
		}
		long t3 = (timestamp.length>0) ? timestamp[0] : System.currentTimeMillis();
		InvokerInvocTrace invocation = (InvokerInvocTrace)TraceContext.innerRpc();
		invocation.setT3(t3);
		if (invocation.getInvoker().getHost() == null) {
			invocation.getInvoker().setHost(NetworkUtils.LOCAL_IP);
		}
	}
	/**
	 * 客户端标识Server处理结束（SS）
	 */
	public static void setT4(Long... timestamp) {
		if (TraceContext.isDisabled()) {
			return;
		}
		long t4 = (timestamp.length>0) ? timestamp[0] : System.currentTimeMillis();
		TraceContext.innerRpc().setT4(t4);
	}
	
//	/**
//	 * 服务端开始处理
//	 */
//	public static void setT3_SR(String rpcId, Invoker invoker, Long... timestamp) {
//		//前提：traceId传过来了
//		TraceContext.initiate(rpcId, invoker);
//		setT3(timestamp);
//	}
//	/**
//	 * 服务端处理结束
//	 */
//	public static void setT4_SS(Long... timestamp) {
//		setT4(timestamp);
//		send();
//	}
	
//	public static String getRpcId() {
//		return TraceContext.getRpcId();
//	}
	public static void setMemo2(String memo, String... method) {
		if (TraceContext.isDisabled()) {
			return;
		}
		TraceContext.innerRpc().setMemo(memo);
	}
	public static void setMethodParams(Object parameters, String... method) {
		if (TraceContext.isDisabled()) {
			return;
		}
		TraceContext.innerRpc().setParameters(parameters);
		if (method.length > 0) {
			TraceContext.getInvoker().setMethod(method[0]);
		}
	}
	public static void markError(Throwable... exception) {
		if (TraceContext.isDisabled()) {
			return;
		}
		TraceContext.innerRpc().setHasErrors(true);
	}
	
	private static void send() {
		InvocTrace trace = TraceContext.popStack();
		getSender().send(trace);
		if (TraceContext.isOver()) {
			TraceContext.clear();
		}
	}
	
	private static TracingSender sender;
	private static TracingSender DEFAULT = new TracingSender() {
		public void send(InvocTrace trace) {
			logger.warn("", trace);
		}
	};
	
	public static TracingSender getSender() {
		return sender==null ? DEFAULT : sender;
	}

}
