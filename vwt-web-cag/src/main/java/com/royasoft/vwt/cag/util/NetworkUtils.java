/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络相关工具类
 * 
 * @author jxue
 * 
 */
public class NetworkUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);

    /**
     * 根据传入的IP前缀标示进行匹配，获取本机IP地址(只支持IPV4)
     * 
     * @param include 参考:192.168
     * @return
     */
    public static String getLocalIP(String... include) {
        Enumeration<NetworkInterface> allNetInterfaces = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();

            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) {
                        for (String ip_prefix : include) {
                            if (ip.getHostAddress().indexOf(ip_prefix) >= 0)
                                return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (java.net.SocketException e) {
            logger.error("获取本机IP异常:{}", e);
        }

        return null;
    }
}
