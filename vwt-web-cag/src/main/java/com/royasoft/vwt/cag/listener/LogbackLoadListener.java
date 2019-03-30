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

import org.apache.commons.io.IOUtils;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;

import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.cag.constant.ZkNodeConstant;

/**
 * @ClassName: LogbackLoadListener
 * @Description: 配置logback
 * @author: xutf
 * @date: 2016年5月5日 下午3:28:01
 */
public class LogbackLoadListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LogbackLoadListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            final ZkUtil zkUtil = event.getApplicationContext().getBean(ZkUtil.class);
            String config = zkUtil.findData(ZkNodeConstant.Logback.ZK_CONFIG);
            loadLogback(config);
            // 配置zk监听
            zkUtil.addDataWatcher(ZkNodeConstant.Logback.ZK_CONFIG, new ZkUtil.ZkDataOp() {
                @Override
                public void process(NodeCache nodeCache) {
                    String config = new String(nodeCache.getCurrentData().getData());
                    loadLogback(config);
                }
            });
        } catch (Exception e) {
            logger.error("加载logback配置异常", e);
        }
    }

    private void loadLogback(String config) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure(IOUtils.toInputStream(config, "utf-8"));
        } catch (Exception e) {
            logger.error("加载logback配置异常", e);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }

}
