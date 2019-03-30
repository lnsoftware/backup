package com.royasoft.vwt.cag.listener;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.cag.constant.ZkNodeConstant;
import com.royasoft.vwt.cag.util.mq.LogRocketMqUtil;
import com.royasoft.vwt.cag.util.mq.RocketMqUtil;
import com.royasoft.vwt.cag.util.mq.UserAndCorpRocketMqUtil;

public class RocketMqLoadListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BaseInfoLoadListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            final ZkUtil zkUtil = event.getApplicationContext().getBean(ZkUtil.class);
            String imMQ = zkUtil.findData(ZkNodeConstant.BaseInfo.ZK_IM_MQ_ADDR);
            String commonMQ = zkUtil.findData(ZkNodeConstant.BaseInfo.ZK_COMMON_MQ_ADDR);
            initImMQ(imMQ);
            initLogMq(commonMQ);
            initUserCorpLogMq(commonMQ);
            // 配置zk监听
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_IM_MQ_ADDR, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    String mqUrl = new String(nodeCache.getCurrentData().getData());
                    initImMQ(mqUrl);
                }
            });
            zkUtil.addDataWatcher(ZkNodeConstant.BaseInfo.ZK_COMMON_MQ_ADDR, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    String mqUrl = new String(nodeCache.getCurrentData().getData());
                    initLogMq(mqUrl);
                    initUserCorpLogMq(mqUrl);
                }
            });
        } catch (Exception e) {
            logger.error("加载logback配置异常", e);
        }
    }

    /**
     * 初始化连接消息推送mq
     * 
     * @param node
     */
    private void initImMQ(String imMQ) {
        if (null != RocketMqUtil.producer)
            RocketMqUtil.producer.shutdown();
        if (null == imMQ || "".equals(imMQ)) {
            logger.error("初始化连接消息推送mq失败.......mq地址为空");
            return;
        }
        logger.debug("初始化连接消息推送mq, mqAddr:{}", imMQ);
        RocketMqUtil.init(imMQ);
    }

    /**
     * 初始化连接日志流水mq
     * 
     * @param node
     */
    private void initLogMq(String logMQ) {
        if (null != LogRocketMqUtil.producer)
            LogRocketMqUtil.producer.shutdown();
        if (null == logMQ || "".equals(logMQ)) {
            logger.error("初始化连接日志流水mq失败.......mq地址为空");
            return;
        }
        logger.debug("初始化连接日志流水mq,mqAddr:{}", logMQ);
        LogRocketMqUtil.init(logMQ);
    }

    /**
     * 初始化连接经分mq
     * 
     * @param node
     */
    private void initUserCorpLogMq(String jingfenMQ) {
        if (null != UserAndCorpRocketMqUtil.producer)
            UserAndCorpRocketMqUtil.producer.shutdown();
        if (null == jingfenMQ || "".equals(jingfenMQ)) {
            logger.error("初始化连接经分mq失败.......mq地址为空");
            return;
        }
        logger.debug("初始化连接经分mq,mqAddr:{}", jingfenMQ);
        UserAndCorpRocketMqUtil.init(jingfenMQ);
    }
}
