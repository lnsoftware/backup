/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.royasoft.vwt.cag.conf.ParamConfig;

/**
 * zk连接工具类
 *
 * @Author:MB
 * @Since:2015年11月20日
 */
@Component
public class ZkConnectUtil {

    private static final Logger logger = LoggerFactory.getLogger(ZkConnectUtil.class);

    private static ZkConnectUtil instance = null;

    private ZkClient zkClient;

    private ZkConnectUtil() {

    }

    public static ZkConnectUtil getInstance() {
        synchronized (ZkConnectUtil.class) {
            if (instance == null) {// 加入同步 instance,zkClient 对象只会new一次
                instance = new ZkConnectUtil();
                instance.initZkClient();
            }
        }
        return instance;
    }

    public void initZkClient() {
        String zkUrl = ParamConfig.im_zk_url;
        logger.debug("连接zk,zkUrl:{}", zkUrl);
        instance.zkClient = new ZkClient(zkUrl);
    }

    public ZkClient getConnect() {
        return zkClient;
    }

}
