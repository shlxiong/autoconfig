package com.openxsl.config.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络设备的IP地址
 * @author xiongsl
 */
public class NetworkUtils {
	private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
	
	public static final String LOCAL_IP;
	
	static {
		LOCAL_IP = getLocalAddress().getHostAddress();
	}
	
	public static String getRequestHost(HttpServletRequest request){
		String[] proxyKeys = {
				//squid, apache, nginx
				"x-forwarded-for", "Proxy-Client-IP", "X-Real-IP", 
				//weblogic, others
				"WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
		};
		String ipAddress = null;
		for (String proxy : proxyKeys) {
			String proxyAddr = request.getHeader(proxy);
			if (isValidProxyAddr(proxyAddr)) {
				ipAddress = proxyAddr;
				break;
			}
		}
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)){
        	ipAddress = LOCAL_IP;
        }
        
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割  
        if (ipAddress!=null && ipAddress.length()>15){
        	int idx = ipAddress.indexOf(",");
            if (idx > 0){  
                ipAddress = ipAddress.substring(0, idx);  
            }  
        }  
        return ipAddress; 
    }
	
	private static InetAddress getLocalAddress() {
        Enumeration<NetworkInterface> interfaces;
        Enumeration<InetAddress> addresses;
        InetAddress uncertain = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
	            while (interfaces.hasMoreElements()) {
	                try {
	                    addresses = interfaces.nextElement().getInetAddresses();
	                    while (addresses.hasMoreElements()) {
                            InetAddress address = addresses.nextElement();
                            if (isValidAddress(address)) {
                            	if (isRealAddress(address)) {  //address.isSiteLocalAddress();
                            		return address;
                            	} else {
                            		uncertain = address;
                            	}
                            }
	                    }
	                } catch (Throwable e) {
	                	logger.warn("Failed to retriving ip address: ", e);
	                }
	            }
            }
        } catch (Throwable e) {
        	logger.warn("Failed to retriving ip address: ", e);
        }finally {
        	addresses = null;
        	interfaces = null;
        }
        
        if (uncertain != null) {
        	logger.info("Could not get localhost ip address exactly, use {} instead.", uncertain.getHostAddress());
        	return uncertain;
        } else {
        	logger.error("Could not get localhost ip address, will use 127.0.0.1 instead.");
        	try {
				return InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				return null;
			}
        }
	}
	
	//A: 10.0.0.0 ~ 10.255.255.255
	static final Pattern INTERNAL_A = Pattern.compile("10.\\d{1,3}.\\d{1,3}.\\d{1,3}");
	//B: 172.16.0.0 ~ 172.31.255.255
	static final Pattern INTERNAL_B = Pattern.compile("172.(1[6-9]|2[0-9]|30|31|).\\d{1,3}.\\d{1,3}");
	//C: 192.168.0.0 ~ 192.168.255.255
	static final Pattern INTERNAL_C = Pattern.compile("192.168.\\d{1,3}.\\d{1,3}");
	public static boolean isInternalAddress(String address){
		if (address == null) {
			throw new IllegalArgumentException("IP地址为空");
		}
		if (!Patterns.HOST_IP.matcher(address).matches()){
			return false;
		}
		
		return "127.0.0.1".equals(address) || INTERNAL_A.matcher(address).matches()
				|| INTERNAL_B.matcher(address).matches()
				|| INTERNAL_C.matcher(address).matches();
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(LOCAL_IP);
		System.out.println(isInternalAddress("172.19.19.25"));
		System.out.println(isInternalAddress("10.50.8.10"));
		System.out.println(isInternalAddress("192.168.1.2"));
		System.out.println(isInternalAddress("218.75.73.58"));
		System.out.println("----------IP-format");
		System.out.println(Patterns.HOST_IP.matcher("218.75.73.258").matches());
		System.out.println(Patterns.HOST_IP.matcher("218.75.73").matches());
		System.out.println(Patterns.HOST_IP.matcher("127.0.0.1").matches());
		
		String a = "aaaaaaaa", b = "bbbbbbbb";
		long current = System.currentTimeMillis();
		for (int i=0; i<680000; i++) {
			new StringBuilder(a).append("#").append(b).toString();
		}
		System.out.println("buffer:" + (System.currentTimeMillis()-current));
		current = System.currentTimeMillis();
		@SuppressWarnings("unused")
		String target = "";
		for (int i=0; i<680000; i++) {
			target = a + "#" + b;  //
		}
		System.out.println("new:" + (System.currentTimeMillis()-current));
		current = System.currentTimeMillis();
		for (int i=0; i<680000; i++) { //慢
			"s1#s2".replace("s1", a).replace("s2", b);
		}
		System.out.println("replace:" + (System.currentTimeMillis()-current));
		current = System.currentTimeMillis();
		for (int i=0; i<680000; i++) {
			String.format("%s#%s", a,b);  //最慢
		}
		System.out.println("format:" + (System.currentTimeMillis()-current));
	}
	
	private static boolean isValidAddress(InetAddress address) {
        if (null == address || address.isLoopbackAddress() //127.*.*.*
        		|| address.isLinkLocalAddress() || address.isAnyLocalAddress()) {
            return false;
        }
        return address.isSiteLocalAddress();
    }
	private static boolean isRealAddress(InetAddress address) {
		String ipv4 = address.getHostAddress();   //IPV4
		String segments = ipv4.substring(0, ipv4.lastIndexOf("."));
		return Patterns.HOST_IP.matcher(ipv4).matches() && segments.length() > 5;
	}
	
	private static final boolean isValidProxyAddr(String proxyAddr){
    	return proxyAddr!=null && proxyAddr.length()>0
    			&& !proxyAddr.equalsIgnoreCase("unknown");
    }
}
