/**   
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @Title: QueueMonitorListener.java
 * @Prject: vwt-web-controller
 * @Package: com.royasoft.vwt.controller.listener
 * @Description: TODO
 * @author: xutf
 * @date: 2016年5月27日 下午4:49:50
 * @version: V1.0
 */
package com.royasoft.vwt.controller.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.royasoft.vwt.controller.service.QueueMonitorService;

/**
 * @ClassName: QueueMonitorListener
 * @Description: TODO
 * @author: xutf
 * @date: 2016年5月27日 下午4:49:50
 */
public class QueueMonitorListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(QueueMonitorListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            QueueMonitorService monitorService = event.getApplicationContext().getBean(QueueMonitorService.class);
            monitorService.startMonitor();
        } catch (Exception e) {
            logger.error("业务队列监控启动出错：", e);
            System.exit(1);
        }
    }

}
