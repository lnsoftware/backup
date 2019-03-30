package com.royasoft.vwt.cag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.royasoft.vwt.cag.queue.ServicesQueue;

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
                    logger.info("通讯录业务队列当前长度为:{} ", ServicesQueue.address_queue.size());
                    logger.info("公告业务队列当前长度为:{} ", ServicesQueue.announce_queue.size());
                    logger.info("积分业务队列当前长度为:{} ", ServicesQueue.integral_queue.size());
                    logger.info("登陆业务队列当前长度为:{} ", ServicesQueue.loginAuth_queue.size());
                    logger.info("会议业务队列当前长度为:{} ", ServicesQueue.meeting_queue.size());
                    logger.info("红包业务队列当前长度为:{} ", ServicesQueue.redpacket_queue.size());
                    logger.info("任务业务队列当前长度为:{} ", ServicesQueue.sendTask_queue.size());
                    logger.info("设置业务队列当前长度为:{} ", ServicesQueue.setting_queue.size());
                    logger.info("版本更新业务队列当前长度为:{} ", ServicesQueue.version_queue.size());
                    logger.info("多角色工作台业务队列 当前长度为:{} ", ServicesQueue.workBench_queue.size());
                    logger.info("工作圈业务队列 当前长度为:{} ", ServicesQueue.WorkTeam_queue.size());
                    logger.info("企业邮箱管理业务队列 当前长度为:{} ", ServicesQueue.mailBox_queue.size());
                    logger.info("企业或部门logo业务队列 当前长度为:{} ", ServicesQueue.corpCustom_queue.size());
                    logger.info("IMS通讯录业务队列 当前长度为:{} ", ServicesQueue.ims_queue.size());
                    logger.info("投票业务队列 当前长度为:{} ", ServicesQueue.vote_queue.size());
                    logger.info("二学一做队列 当前长度为:{} ", ServicesQueue.twoLearn_queue.size());
                    logger.info("美丽江苏队列 当前长度为:{} ", ServicesQueue.beautyJS_queue.size());
                    logger.info("同步敏感词队列 当前长度为:{} ", ServicesQueue.sensitiveword_queue.size());
                    logger.info("同步免打扰状态业务队列 当前长度:{}", ServicesQueue.noDisturb_queue.size());
                    logger.info("山东收藏业务队列 当前长度:{}", ServicesQueue.conllection_queue.size());
                    logger.info("山东pc版本更新业务队列 当前长度:{}", ServicesQueue.versionPc_queue.size());
                    logger.info("山东OA接口更新业务队列 当前长度:{}", ServicesQueue.shandongoa_queue.size());
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                    }
                }

            }
        }).start();
    }
}
