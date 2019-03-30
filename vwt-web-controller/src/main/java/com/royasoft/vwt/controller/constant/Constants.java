/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.constant;


public class Constants {

    /** 登录缓存信息 */
    public static final String nameSpace = "ROYASOFT:VWT:OMC:SESSIONID:";
    
    public static final String ANNOUNCECODE_NAMESPACE="ROYASOFT:VWT:ANNOUNCE:CODE:";

    public final static String fastDFSNode = "/royasoft/vwt/cag/file_server_url";
    /** 文件服务器 */
    public static final String FILE_URL = "/royasoft/imserver/fastdfs_nginx";
    /** 操作成功 */
    public static final String ACTION_SUCCESS = "操作成功";
    /** 操作失败 */
    public static final String ACTION_FAIL = "操作失败";

    /** dictionary的id值51 */
    public static final long DICTIONARYID = 51;
    /** dictionary的id值61 企业人数*/
    public static final long DICTIONARYCORPCOUNT = 61;
    /** 地市管理员角色 */
    public static final String DISHIADMIN = "5";
    /** 区县管理员角色 */
    public static final String QUXIANADMIN = "6";
    /** 客户经理角色 */
    public static final String CUSTOMADMIN = "7";

    /**短信发送地市区县及客户经理内容*/
    public static final String sendCityContent="您收到一个V网通企业注册申请，请尽快登录管理平台处理！【V网通】";
    /**客户经理审核未通过短信*/
    public static final String sendNoPassContent="【V网通】您的认证申请未通过审核，如需加入请完善认证信息或与您的集团客户经理联系，感谢您的申请!";
    /**开户成功短信1*/
    public static final String sendOpenContent1="【V网通】您已通过V网通认证申请，可以登录电脑后台管理通讯录（http://112.4.17.117/omc/#），您的管理账号:"; 
    /**开户成功短信1*/
    public static final String sendOpenContent2="，密码:";
    /**开户成功短信1*/
    public static final String sendOpenContent3="（为了保证账号安全，切勿将账号信息透露给他人）";
    
    public static final class Logback {
        /** zk home路径 **/
        public static final String ZK_HOME = "/royasoft/vwt/logback";
        /** scheduler日志配置路径 **/
        public static final String ZK_CONFIG = ZK_HOME + "/controller";
    }

    public static final class Param {
        public static final String ZK_HOME = "/royasoft/vwt/controller";

        public static final String ZK_PORT = ZK_HOME + "/port";
        
        /**文件服务器访问地址*/
        public static final String FILE_SERVER_URL="/royasoft/vwt/cag/file_server_url";
        
        public static final String NGINX_ADDRESS = "/royasoft/vwt/cag/nginx_address";
        
        public static final String ZK_IM_MQ_URL="/royasoft/imserver/im_mq/address";
        
        //素材图文地址
        public static final String ZK_GRAPHIC_SOURCE_URL = "/royasoft/vwt/omc/graphic_source_url";
        
        //素材推送到微信
        public static final String ZK_WECHAT_URL = "/royasoft/comp/esip/url";
        
        public static final String ZK_WECHAT_AES_KEY = "/royasoft/comp/esip/weChatAesKey";
        
     // FastDfs 节点
        public static final String ZK_FASTDFS_NGINX= "/royasoft/imserver/fastdfs_nginx";
        
        public final static String GRAPHIC_SOURCE_URL = "/royasoft/vwt/omc/graphic_source_url";
        
        public final static String GRAPHIC_SOURCE_URL_WECHAT = "/royasoft/vwt/omc/graphic_source_url_wechat";
    }
    
    /**
     * 部门管理员操作 
     * v3.2.0
     * @author hejinhu
     *
     */
    public static final class DeptManager{
        /**
         * 部门管理员权限
         */
        public static final String DEPT_ROLE = "8";
        /**
         * 应用管理员权限
         */
        public static final String SQUARE_ROLE = "11";
    }
    
}
