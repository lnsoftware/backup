/**   
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @Title: LogbackLoadListener.java
 * @Prject: vwt-base-services
 * @Package: com.royasoft.vwt.soa.base
 * @Description: TODO
 * @author: xutf
 * @date: 2016年5月5日 下午3:28:01
 * @version: V1.0
 */
package com.royasoft.vwt.cag.listener;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.constant.ZkNodeConstant;
import com.royasoft.vwt.cag.util.SensitivewordFilter;
import com.royasoft.vwt.cag.util.upload.FastDFSUtil;

/**
 * 基础信息配置类
 *
 * @Author:MB
 * @Since:2016年5月27日
 */
public class BaseInfoLoadListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BaseInfoLoadListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            final ZkUtil zkUtil = event.getApplicationContext().getBean(ZkUtil.class);
            String config = zkUtil.findData(ZkNodeConstant.BaseInfo.ZK_FASTDFS);
            loadFastDfs(config);
            initBaseInfo(zkUtil);
            /************************对于文件服务器地址监听****************************/
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_FASTDFS, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    String config = new String(nodeCache.getCurrentData().getData());
                    loadFastDfs(config);
                }
            });
            /************************对于语音网关监听****************************/
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_VOICE_URL, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.voice_url = new String(nodeCache.getCurrentData().getData());
                }
            });
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_VOICE_ACCOUNTID, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.voice_account_id = new String(nodeCache.getCurrentData().getData());
                }
            });
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_VOICE_APPID, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.voice_app_id = new String(nodeCache.getCurrentData().getData());
                }
            });
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_VOICE_DISPLAY_NUM, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.voice_display_num = new String(nodeCache.getCurrentData().getData());
                }
            });
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_VOICE_TOKEN, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.voice_token = new String(nodeCache.getCurrentData().getData());
                }
            });
            /************************对于移动审批H5应用id监听****************************/
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_APPROVAL_ID, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.approval_id = new String(nodeCache.getCurrentData().getData());
                }
            });
            /************************对于敏感词监听****************************/
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_sensitiveword, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.sensitivewords = new String(nodeCache.getCurrentData().getData());
                    SensitivewordFilter.getInstance().init();
                }
            });
            /************************对于敏感词开关监听****************************/
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_ISFILTERSENSITIVEWORD, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.isFilterSensitivewords = new String(nodeCache.getCurrentData().getData());
                }
            });
            /************************对于推送社区助手监听****************************/
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_FIRST_LOGIN, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.first_login_content = new String(nodeCache.getCurrentData().getData());
                }
            });
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_COM_BACK, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.come_back_content = new String(nodeCache.getCurrentData().getData());
                }
            });
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_SERVICEID_NO, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.v_group_service_no = new String(nodeCache.getCurrentData().getData());
                }
            });
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_SECURITY_KEY, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    ParamConfig.SecurityKey = new String(nodeCache.getCurrentData().getData());
                }
            });
        } catch (Exception e) {
            logger.error("加载logback配置异常", e);
        }
    }

    private void loadFastDfs(String config) {
        FastDFSUtil.init(config);
    }

    private void initBaseInfo(ZkUtil zkUtil) {
        ParamConfig.appstore_enforce = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_APPSTORE_ENFORCE);
        ParamConfig.appstore_url = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_APPSTORE_URL);
        ParamConfig.cag_port = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_CAG_PORT);
        ParamConfig.common_user_timeout = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_COMMON_USER_TIMEOUT);
        ParamConfig.customer_no = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_CUSTOMER_NO);
        ParamConfig.file_server_url = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_FILE_SERVER_URL);
        ParamConfig.ftp_ip = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_FTP_IP);
        ParamConfig.ftp_port = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_FTP_PORT);
        ParamConfig.ftp_pwd = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_FTP_PWD);
        ParamConfig.ftp_username = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_FTP_USERNAME);
        ParamConfig.im_zk_url = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_IM_ZK_URL);
        ParamConfig.ims_cmd_cancel = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_IMS_CMD_CANCLE);
        ParamConfig.ims_cmd_open = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_IMS_CMD_OPEN);
        ParamConfig.ims_pwd = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_IMS_PASSWORD);
        ParamConfig.ims_url = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_IMS_URL);
        ParamConfig.ims_useagent = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_IMS_USEAGENT);
        ParamConfig.ims_verifycode = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_IMS_VERIFYCODE);
        ParamConfig.internet_part_name = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_INTERNET_PART_NAME);
        ParamConfig.internet_user_timeout = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_INTERNET_USER_TIMEOUT);
        ParamConfig.nginx_address = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_NGINX_ADDRESS);
        ParamConfig.rsa_private_key = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_RSA_PRIVATE_KEY);
        ParamConfig.share_url = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_SHARE_URL);
        ParamConfig.sso_url = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_SSO_URL);
        ParamConfig.surf_no = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_SURF_NO);
        ParamConfig.verify_code_timeout = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_VERIFYCODE_TIMEOUT);
        ParamConfig.vfc_url = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_VFC_URL);
        ParamConfig.ABOUTVWT_ID = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_ABOUTVWT_ID);
        ParamConfig.ABOUTVWT_TIPS = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_ABOUTVWT_TIPS);
        ParamConfig.number_limit = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_NUMBER_LIMIT);
        ParamConfig.voice_account_id = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_VOICE_ACCOUNTID);
        ParamConfig.voice_app_id = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_VOICE_APPID);
        ParamConfig.voice_display_num = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_VOICE_DISPLAY_NUM);
        ParamConfig.voice_token = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_VOICE_TOKEN);
        ParamConfig.voice_url = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_VOICE_URL);
        ParamConfig.approval_id=findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_APPROVAL_ID);
        ParamConfig.sms_daily_count=findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_SMS_DAILY_COUNT);
        ParamConfig.sensitivewords=findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_sensitiveword);
        ParamConfig.isFilterSensitivewords=findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_ISFILTERSENSITIVEWORD);
        ParamConfig.GRAPHIC_SOURCE_URL = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_GRAPHIC_SOURCE_URL);
        ParamConfig.first_login_content=findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_FIRST_LOGIN);
        ParamConfig.come_back_content=findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_COM_BACK);
        ParamConfig.v_group_service_no = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_SERVICEID_NO);
        ParamConfig.SecurityKey = findValueFromZK(zkUtil, ZkNodeConstant.BaseInfo.ZK_SECURITY_KEY);
    }
    
    private String findValueFromZK(ZkUtil zkUtil, String nodeName) {
        try {
            return zkUtil.findData(nodeName);
        } catch (Exception e) {
            return null;
        }
    }

}
