/**   
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @Title: LogbackLoadListener.java
 * @Prject: vwt-base
 * @Package: com.royasoft.vwt.soa.base
 * @Description: TODO
 * @author: xutf
 * @date: 2016年5月5日 下午3:28:01
 * @version: V1.0
 */
package com.royasoft.vwt.controller.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.controller.config.ParamConfig;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.util.LogRocketMqUtil;
import com.royasoft.vwt.controller.util.RocketMqUtil;
import com.royasoft.vwt.controller.util.upload.FastDFSUtil;

/**
 * @ClassName: LogbackLoadListener
 * @Description: 配置文件系统和消息队列
 * @author: xutf
 * @date: 2016年5月5日 下午3:28:01
 */
public class InitListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(InitListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            ZkUtil zkUtil = event.getApplicationContext().getBean(ZkUtil.class);
            initLogMq(zkUtil);
            initImMq(zkUtil);
//            initFastDFS(zkUtil);
            initBaseInfo(zkUtil);
        } catch (Exception e) {
            logger.error("配置文件系统和消息队列出错：", e);
            System.exit(1);
        }
    }

    private void initLogMq(ZkUtil zkUtil) throws Exception {
        String mqNode = "/royasoft/vwt/mq/address";
        String nameAddr = zkUtil.findData(mqNode);
        logger.info("初始化链接LogMQ, mq_node:{},mqAddr:{}", mqNode, nameAddr);
        // 启动消息队列
        LogRocketMqUtil.init(nameAddr);
        // 加入退出钩子
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LogRocketMqUtil.shutdown();
            }
        });
    }

    private void initFastDFS(ZkUtil zkUtil) throws Exception {
        String fastDFSNode = "/royasoft/imserver/fastdfs";
        String trackerAddr = zkUtil.findData(fastDFSNode);
        logger.info("初始化FastDFS地址:{}:", trackerAddr);
        FastDFSUtil.init(trackerAddr);
    }
    
    private void initImMq(ZkUtil zkUtil) throws Exception {
        String nameAddr = zkUtil.findData(Constants.Param.ZK_IM_MQ_URL);
        logger.info("初始化链接LogMQ, mq_node:{},mqAddr:{}", Constants.Param.ZK_IM_MQ_URL, nameAddr);
        // 启动消息队列
        RocketMqUtil.init(nameAddr);
        // 加入退出钩子
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                RocketMqUtil.shutdown();
            }
        });
    }
    private void initBaseInfo(ZkUtil zkUtil) throws Exception {
        ParamConfig.FILE_SERVER_URL = zkUtil.findData(Constants.Param.FILE_SERVER_URL);
        ParamConfig.NGINX_ADDRESS = zkUtil.findData(Constants.Param.NGINX_ADDRESS);
        ParamConfig.GRAPHIC_SOURCE_URL = zkUtil.findData(Constants.Param.ZK_GRAPHIC_SOURCE_URL);
    }

}
