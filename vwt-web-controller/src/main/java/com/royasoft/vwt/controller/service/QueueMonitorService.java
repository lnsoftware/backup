package com.royasoft.vwt.controller.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.royasoft.vwt.controller.queue.ServicesQueue;

/**
 * 业务队列监控
 *
 * @Author:MB
 * @Since:2016年4月22日
 */
@Component
public class QueueMonitorService {

    private final Logger logger = LoggerFactory.getLogger(QueueMonitorService.class);

    /**
     * 开始监控
     * 
     * @Description:
     */
    public void startMonitor() {
        logger.info("业务队列监控开始......");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    logger.info("登陆业务队列当前长度为:{} ", ServicesQueue.loginAuth_queue.size());
                    logger.info("素材中心业务队列当前长度为:{} ", ServicesQueue.materialCenter_queue.size());
                    logger.info("投票业务队列当前长度为:{} ", ServicesQueue.vote_queue.size());
                    logger.info("用户意见反馈队列当前长度为:{} ", ServicesQueue.useFeedback_queue.size());
                    logger.info("互联网认证队列当前长度为:{} ", ServicesQueue.internetAuth_queue.size());
                    logger.info("通讯录业务队列 当前长度为:{} ", ServicesQueue.address_queue.size());
                    logger.info("公告业务队列 当前长度为:{} ", ServicesQueue.announce_queue.size());
                    logger.info("Redis业务队列 当前长度为:{} ", ServicesQueue.redis_queue.size());
                    logger.info("两学一做队列当前长度为:{} ", ServicesQueue.twoLearn_queue.size());
                    logger.info("圈子管理队列当前长度为:{} ", ServicesQueue.circle_queue.size());
                    logger.info("关键词队列当前长度为:{} ", ServicesQueue.keyWords_queue.size());
                    logger.info("服务号信息队列当前长度为:{}", ServicesQueue.squareMessage_queue.size());
                    logger.info("群聊队列当前长度为:{} ", ServicesQueue.imGroup_queue.size());
                    logger.info("百度富文本上传图片队列当前长度为:{} ", ServicesQueue.baiduUpload_queue.size());
                    logger.info("节日欢迎图队列当前长度为:{} ", ServicesQueue.festival_queue.size());
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }
}
