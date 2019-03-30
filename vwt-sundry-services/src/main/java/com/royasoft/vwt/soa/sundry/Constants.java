/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry;

/**
 * 常量类
 * 
 * 常量类提供redis、jdbc的相关功能
 * 
 * @author yujun
 */
public class Constants {

    /** zk上配置的redis集群地址 **/
    public static final String ZK_REDIS_HOME = "/royasoft/vwt/redis";
    
    /** zk上配置的jdbc信息 **/
    public static final String ZK_JDBC_HOME = "/royasoft/vwt/jdbc_user";
    
    public static final String ZK_INSIDE_MAX_COUNT="/royasoft/vwt/cag/inside/max_count";
    
    public static final String ZK_INSIDE_MAX_EVERY_COUNT="/royasoft/vwt/cag/inside/max_every_count";

    public static final String ZK_INSIDE_END_TIME="/royasoft/vwt/cag/inside/end_time";
}
