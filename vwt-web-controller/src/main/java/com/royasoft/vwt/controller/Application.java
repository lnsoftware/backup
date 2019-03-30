/**   
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @author: xutf
 * @date: 2016年5月6日 下午6:02:37
 * @version: V1.0
 */
package com.royasoft.vwt.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.royasoft.vwt.base.listener.LogbackLoadListener;
import com.royasoft.vwt.controller.config.ParamConfig;
import com.royasoft.vwt.controller.constant.Constants;
import com.royasoft.vwt.controller.listener.InitListener;
import com.royasoft.vwt.controller.listener.QueueMonitorListener;
import com.royasoft.vwt.controller.listener.ThreadPoolListener;
import com.royasoft.vwt.controller.server.HttpServer;

/**
 * @ClassName: Application
 * @Description: 启动应用
 * @author: xutf
 * @date: 2016年5月6日 下午6:02:37
 */

@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String SERVICES_NAME = "controller services";

    public static void main(String[] args) {
        try {
            log.info(SERVICES_NAME + "开始启动...");
            SpringApplication app = new SpringApplication(Application.class);
            app.addListeners(new InitListener());
            app.addListeners(new LogbackLoadListener(Constants.Logback.ZK_CONFIG));
            app.addListeners(new ThreadPoolListener());
            app.addListeners(new QueueMonitorListener());
            app.run(args);
            HttpServer.run(ParamConfig.port);
            log.info(SERVICES_NAME + "完成启动...");
            while (true) {
                Thread.sleep(60 * 1000);
                log.info(SERVICES_NAME + "运行中...");
            }
        } catch (Throwable e) {
            log.error(SERVICES_NAME + "启动失败", e);
        }
    }

}
