package com.royasoft.vwt.controller.util;

public class BaseConstant {
    
    public final static String fastDFSNode = "/royasoft/imserver/fastdfs_nginx";
    
    public final static String fastDFSADDR = "/royasoft/imserver/fastdfs";

    public final static String NGINX_INNER_ADDRESS = "/royasoft/vwt/nginx_inner_address";
    
    public final static String fastDFSInnerStartNode = "/royasoft/imserver/fastdfs_inner_start_nginx";
    
    public final static String NGINX_ADDRESS = "/royasoft/vwt/nginx_address";
    
    public final static String fastDFSInnerNode = "/royasoft/imserver/fastdfs_inner_nginx";
    
    
    /** 缓存命名空间,key为:ROYASOFT:VWT:OMC:SESSIONID:sessionId */
    public static final String nameSpace = "ROYASOFT:VWT:OMC:SESSIONID:";
    
 // 角色相关常量
    /**
     * 角色 - 系统管理员
     */
    public static final int ROLENAME_ADMIN_SYSTEM = 1;
    /**
     * 角色 - 平台管理员
     */
    public static final int ROLENAME_ADMIN_PLATFORM = 2;
    /**
     * 角色 - 企业管理员
     */
    public static final int ROLENAME_ADMIN_CORP = 3;
    /**
     * 角色 - 省公司管理员
     */
    public static final int ROLENAME_ADMIN_PROVINCE = 4;
    /**
     * 角色 - 地市公司管理员
     */
    public static final int ROLENAME_ADMIN_CITY = 5;
    /**
     * 角色 - 区县管理员
     */
    public static final int ROLENAME_ADMIN_AREA = 6;
    /**
     * 角色 -客户经理
     */
    public static final int ROLENAME_ADMIN_CUSTOMER = 7;
    
    /**
     * 角色 -部门管理员
     */
    public static final int ROLENAME_ADMIN_DEPT = 8;
    
    /**
     * 应用,素材 -素材标题为主标题
     */
    public static final String GRAPHIC_IS_MAIN = "1";// 素材标题为主标题

    /**
     * 应用,素材 -素材标题为副标题
     */
    public static final String GRAPHIC_NOT_MAIN = "0";// 素材标题为副标题

    /**
     * 应用,素材 -素材链接类型为url
     */
    public static final String GRAPHIC_SOURCE_TYPE_URL = "1";// 素材链接类型为url

    /**
     * 应用,素材 -素材链接类型为自定义
     */
    public static final String GRAPHIC_SOURCE_TYPE_CUSTOM = "0";// 素材链接类型为自定义
    
    /**
     * dfs图片文件下载分割路径前段长度
     */
    public static final int DFS_BEFORE = 7;

}
