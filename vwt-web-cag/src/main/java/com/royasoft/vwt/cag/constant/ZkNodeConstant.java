package com.royasoft.vwt.cag.constant;

/**
 * CAG配置zk节点常量类
 *
 * @Author:MB
 * @Since:2016年5月27日
 */
public class ZkNodeConstant {

    /** zk上配置的基础信息 **/
    public static final class BaseInfo {
        public static final String ZK_HOME = "/royasoft/vwt/cag";

        /** fastDfs地址 **/
        public static final String ZK_FASTDFS = ZK_HOME + "/fast_dfs";
        /** nginx地址 **/
        public static final String ZK_NGINX_ADDRESS = ZK_HOME + "/nginx_address";
        /** 新消息服务器ZK地址 **/
        public static final String ZK_IM_ZK_URL = ZK_HOME + "/im_zk_url";
        /** RSA私钥 **/
        public static final String ZK_RSA_PRIVATE_KEY = ZK_HOME + "/rsa_private_key";
        /** VFC_URL **/
        public static final String ZK_VFC_URL = ZK_HOME + "/vfc_url";
        /** SSO_URL **/
        public static final String ZK_SSO_URL = ZK_HOME + "/sso_url";
        /** shareUrl分享链接 **/
        public static final String ZK_SHARE_URL = ZK_HOME + "/share_url";
        /** appStoreUrl **/
        public static final String ZK_APPSTORE_URL = ZK_HOME + "/appstore_url";
        /** appStore_enforce **/
        public static final String ZK_APPSTORE_ENFORCE = ZK_HOME + "/appstore_enforce";
        /** 新文件服务器地址 **/
        public static final String ZK_FILE_SERVER_URL = ZK_HOME + "/file_server_url";
        /** cag端口 **/
        public static final String ZK_CAG_PORT = ZK_HOME + "/cag_port";
        /** FTP-IP **/
        public static final String ZK_FTP_IP = ZK_HOME + "/ftp_ip";
        /** FTP端口 **/
        public static final String ZK_FTP_PORT = ZK_HOME + "/ftp_port";
        /** FTP用户名 **/
        public static final String ZK_FTP_USERNAME = ZK_HOME + "/ftp_username";
        /** FTP密码 **/
        public static final String ZK_FTP_PWD = ZK_HOME + "/ftp_pwd";
        /** IMS地址 **/
        public static final String ZK_IMS_URL = ZK_HOME + "/ims_url";
        /** IMS密码 **/
        public static final String ZK_IMS_PASSWORD = ZK_HOME + "/ims_pwd";
        /** ims_cmd_open **/
        public static final String ZK_IMS_CMD_OPEN = ZK_HOME + "/ims_cmd_open";
        /** ims_cmd_cancel **/
        public static final String ZK_IMS_CMD_CANCLE = ZK_HOME + "/ims_cmd_cancel";
        /** ims验证码 **/
        public static final String ZK_IMS_VERIFYCODE = ZK_HOME + "/ims_verifycode";
        /** ims_useagent **/
        public static final String ZK_IMS_USEAGENT = ZK_HOME + "/ims_useagent";
        /** 我的客户经理服务号id **/
        public static final String ZK_CUSTOMER_NO = ZK_HOME + "/customer_no";
        /** 冲浪新闻服务号id **/
        public static final String ZK_SURF_NO = ZK_HOME + "/surf_no";
        /** 互联网超时时间 **/
        public static final String ZK_INTERNET_USER_TIMEOUT = ZK_HOME + "/internet_user_timeout";
        /** 普通设置超时时间 **/
        public static final String ZK_COMMON_USER_TIMEOUT = ZK_HOME + "/common_user_timeout";
        /** 验证码超时时间 **/
        public static final String ZK_VERIFYCODE_TIMEOUT = ZK_HOME + "/verify_code_timeout";
        /** 互联网创建部门部门名称 **/
        public static final String ZK_INTERNET_PART_NAME = ZK_HOME + "/internet_part_name";

        public static final String ZK_ABOUTVWT_TIPS = ZK_HOME + "/aboutvwt_tips";

        public static final String ZK_ABOUTVWT_ID = ZK_HOME + "/aboutvwt_id";

        public static final String ZK_NUMBER_LIMIT = ZK_HOME + "/number_limit";

        /** 消息推送mq */
        public static final String ZK_IM_MQ_ADDR = "/royasoft/vwt/im_mq/address";

        /** 普通mq */
        public static final String ZK_COMMON_MQ_ADDR = "/royasoft/vwt/mq/address";
        /** 语音-商户id */
        public static final String ZK_VOICE_ACCOUNTID = ZK_HOME + "/voice_account_id";
        /** 语音-调用地址 */
        public static final String ZK_VOICE_URL = ZK_HOME + "/voice_url";
        /** 语音-产品id */
        public static final String ZK_VOICE_APPID = ZK_HOME + "/voice_app_id";
        /** 语音-令牌 */
        public static final String ZK_VOICE_TOKEN = ZK_HOME + "/voice_token";
        /** 语音-显示号码 */
        public static final String ZK_VOICE_DISPLAY_NUM = ZK_HOME + "/voice_display_num";

        public static final String ZK_APPROVAL_ID = ZK_HOME + "/approval_id";

        public static final String ZK_SMS_DAILY_COUNT = ZK_HOME + "/sms_daily_count";
        /**敏感词 **/
        public static final String ZK_sensitiveword = "/royasoft/imserver/sensitiveword/value";
        
        /**敏感词开关 **/
        public static final String ZK_ISFILTERSENSITIVEWORD = "/royasoft/imserver/sensitiveword/isfilter";
        
        /** 素材图文地址 */
        public static final String ZK_GRAPHIC_SOURCE_URL = "/royasoft/vwt/omc/graphic_source_url";
        
        /** 第一次登陆语 */
        public static final String ZK_FIRST_LOGIN = "/royasoft/vwt/cag/first_login_content";
        /** 再次回来欢迎语 */
        public static final String ZK_COM_BACK= "/royasoft/vwt/cag/come_back_content";
        /**服务号Id */
        public static final String ZK_SERVICEID_NO = "/royasoft/vwt/cag/v_group_service_no";
        
        /**查询securitykey */
        public static final String ZK_SECURITY_KEY = "/royasoft/vwt/cag/securityKey";
    }

    /** zk上配置的日志信息 **/
    public static final class Logback {
        /** zk home路径 **/
        public static final String ZK_HOME = "/royasoft/vwt/logback";
        /** cag日志配置路径 **/
        public static final String ZK_CONFIG = ZK_HOME + "/cag";

    }
    
    public static final class InterfaceInfo {
        /** zk 接口信息路径 **/
        public static final String ZK_CAG_INTERFACE_INFO = "/royasoft/vwt/cag/interface/functions";

        public static final String ZK_CAG_INTERFACE_URL_INFO = "/royasoft/vwt/cag/interface/url";
    }
}
