/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * openfire连接进程
 *
 * @Author:MB
 * @Since:2015年11月20日
 */
@Service
@Scope("singleton")
public class MQConnectProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(MQConnectProcessor.class);

//    /** activeMQ地址 */
//    @Value("#{configProperties['activeMQUrl']}")
//    private String activeMQUrl;

    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {// root application context 没有parent，他就是老大.
            // 需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
            new Thread() {
                public void run() {
                    logger.debug("初始化activeMQ连接");
//                    MQProvideUtil.init(activeMQUrl);
                }
            }.start();
        }
    }
}