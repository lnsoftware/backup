/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.alibaba.dubbo.config.ProtocolConfig;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * vwt sundry services 启动类
 * 
 * 杂项服务提供vprivilege的相关功能
 * 
 * @author yujun
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /** 服务名称 **/
    private static final String server_name = "vwt-sundry-services(VWT杂项服务)";

    public static void main(String[] args) throws Exception {
        logger.info(server_name + "启动...");

        while (true) {
            try {
                String launchPath = getLaunchPath();

                loadLogback(launchPath);// 加载logback

                final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("file:" + launchPath + File.separator + "conf" + File.separator + "spring-config.xml");
                context.start();

                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        logger.info(server_name + "停止...");
                        ProtocolConfig.destroyAll();// 将dubbo创建的的非Daemon线程关闭
                        context.close();
                    }
                }));
                logger.info(server_name + "启动完成...");

                while (true) {
                    Thread.sleep(60 * 1000);
                    logger.info(server_name + "运行中...");
                }
            } catch (Exception e) {
                if (e.getMessage().indexOf("Failed to bind to") > 0) {
                    logger.debug("端口号被占用,重新选择端口号...");
                } else {
                    logger.error(server_name + "启动失败", e);
                    break;
                }
            }
        }
    }

    /**
     * 获取程序运行路径
     * 
     * @return 程序当前绝对路径
     * @throws UnsupportedEncodingException
     */
    private static String getLaunchPath() throws UnsupportedEncodingException {
        String filePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        File file = new File(filePath);
        String parentPath = URLDecoder.decode(file.getParentFile().getAbsolutePath(), "utf-8");

        return parentPath;
    }

    /**
     * 加载logback.xml日志配置
     * 
     * @param 程序当前绝对路径
     */
    private static void loadLogback(String path) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure(path + File.separator + "conf" + File.separator + "logback.xml");
        } catch (JoranException e) {
            logger.error("加载logback配置文件异常", e);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }
}
