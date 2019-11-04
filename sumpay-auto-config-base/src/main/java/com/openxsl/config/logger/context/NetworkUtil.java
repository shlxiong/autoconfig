package com.openxsl.config.logger.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author heyc
 * @see autoconfig-dal!NetworkUtils
 */
public class NetworkUtil {

    private static Logger logger = LoggerFactory.getLogger(NetworkUtil.class);

    /**
     * 获取内网IP
     * @return 内网IP
     */
    public static String getSiteIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("getSiteIp error", e);
        }
        return null;
    }

    /**
     * 转换16进制
     * @param ip
     * @return
     */
    public static String ip2HexString(String ip){
        if (isValidIp(ip)){
            String[] ips = ip.split("\\.");
            return ipSegFormat(ips[0]) + ipSegFormat(ips[1]) + ipSegFormat(ips[2]) + ipSegFormat(ips[3]);
        }
        return "7f000001";
    }

    /**
     * isValidIp
     * @param ip
     * @return
     */
    private static boolean isValidIp(String ip){
        return !(ip == null || "".equals(ip) || ip.contains(":"));
    }

    /**
     * ipSegFormat
     * @param seg
     * @return
     */
    private static String ipSegFormat(String seg){
        return String.format("%02x",Integer.valueOf(seg));
    }
}