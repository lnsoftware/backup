package com.royasoft.vwt.cag.conf;

import java.util.HashMap;
import java.util.Map;

import com.royasoft.vwt.cag.vo.InterfaceVo;

/**
 * zk读取的参数
 * 
 * @Author:MB
 * @Since:2016年5月28日
 */
public class ParamConfig {

    /** nginx地址 **/
    public static String nginx_address;
    /** 新消息服务器ZK地址 **/
    public static String im_zk_url;
    /** RSA私钥 **/
    public static String rsa_private_key;
    /** VFC_URL **/
    public static String vfc_url;
    /** SSO_URL **/
    public static String sso_url;
    /** shareUrl分享链接 **/
    public static String share_url;
    /** appStoreUrl **/
    public static String appstore_url;
    /** appStore_enforce **/
    public static String appstore_enforce;
    /** 新文件服务器地址 **/
    public static String file_server_url;
    /** cag端口 **/
    public static String cag_port;
    /** FTP-IP **/
    public static String ftp_ip;
    /** FTP端口 **/
    public static String ftp_port;
    /** FTP用户名 **/
    public static String ftp_username;
    /** FTP密码 **/
    public static String ftp_pwd;
    /** IMS地址 **/
    public static String ims_url;
    /** IMS密码 **/
    public static String ims_pwd;
    /** ims_cmd_open **/
    public static String ims_cmd_open;
    /** ims_cmd_cancel **/
    public static String ims_cmd_cancel;
    /** ims验证码 **/
    public static String ims_verifycode;
    /** ims_useagent **/
    public static String ims_useagent;
    /** 我的客户经理服务号id **/
    public static String customer_no;
    /** 冲浪新闻服务号id **/
    public static String surf_no;
    /** 互联网超时时间 **/
    public static String internet_user_timeout;
    /** 普通设置超时时间 **/
    public static String common_user_timeout;
    /** 验证码超时时间 **/
    public static String verify_code_timeout;
    /** 互联网创建部门部门名称 **/
    public static String internet_part_name;
    
    public static String ABOUTVWT_ID;
    
    public static String ABOUTVWT_TIPS;
    
    /** 群组上限人数 */
    public static String number_limit;
    
    /** 语音-商户id */
    public static  String voice_account_id;
    /** 语音-调用地址 */
    public static  String voice_url;
    /** 语音-产品id */
    public static  String voice_app_id;
    /** 语音-令牌 */
    public static  String voice_token;
    /** 语音-显示号码 */
    public static  String voice_display_num;
    
    /** 每天允许下发短信验证码次数 */
    public static String sms_daily_count;
    
    /** 移动审批H5应用id */
    public static String approval_id;
    /** 素材自定义地址 */
    public static String GRAPHIC_SOURCE_URL;
    
    /** 敏感词**/
    public static String sensitivewords;
    
    /** 敏感词开关**/
    public static String isFilterSensitivewords;
    
    /** 第一次登陆语**/
    public static String first_login_content;
    
    /** 再次回来欢迎语**/
    public static String come_back_content;
    
    /** 服务号Id**/
    public static String v_group_service_no;
    
    /** 服务号Id**/
    public static String SecurityKey;
    
    public static Map<String, InterfaceVo> interfaceVoMap = new HashMap<String, InterfaceVo>();
    
}
