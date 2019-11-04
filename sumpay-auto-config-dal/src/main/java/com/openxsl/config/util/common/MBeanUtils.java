package com.openxsl.config.util.common;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
//import com.sun.management.OperatingSystemMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;

import com.openxsl.config.util.NetworkUtils;

public class MBeanUtils {
	private static MBeanServer platform;
	static{
		platform = ManagementFactory.getPlatformMBeanServer();
	}
	
	public static String getPID(){
		final RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
		//pid@host
//		rtb.getSystemProperties()
		return rtb.getName().split("@")[0];
	}
	
	/**
	 * <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" URIEncoding="UTF-8"/>
	 * @return
	 */
	public static String getEndPoint() throws JMException{
		Set<ObjectName> objs = platform.queryNames(new ObjectName("*:type=Connector,*"),
	            Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
//	    String hostname = InetAddress.getLocalHost().getHostName();
//	    InetAddress[] addresses = InetAddress.getAllByName(hostname);
		StringBuilder endpoint = new StringBuilder();
	    for (Iterator<ObjectName> i = objs.iterator(); i.hasNext(); ) {  
	        ObjectName obj = i.next();
	        String scheme = platform.getAttribute(obj, "scheme").toString();
	        endpoint.append(scheme).append("://").append(NetworkUtils.LOCAL_IP);
	        String port = obj.getKeyProperty("port");
	        endpoint.append(":").append(port);
	    }
	    return endpoint.toString();
	}
	
	public void getMemory(){
//		ManagementFactory.getPlatformMXBean(mxbeanInterface)
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean(); //java.lang:type=Memory
		MemoryUsage heap = mbean.getHeapMemoryUsage();
		heap.getMax();
		heap.getUsed();
		Runtime runtime = Runtime.getRuntime();
		runtime.freeMemory();
		runtime.totalMemory();
	}
	
	public void foo(){  //java.lang:type=OperatingSystem
		OperatingSystemMXBean machine = ManagementFactory.getOperatingSystemMXBean();
		machine.getSystemLoadAverage();
		machine.getAvailableProcessors();
	}

}
