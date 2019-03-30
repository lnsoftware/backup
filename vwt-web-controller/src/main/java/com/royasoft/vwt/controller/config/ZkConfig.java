/**   
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @Title: ZkConfig.java
 * @Prject: vwt-base-services
 * @Package: com.royasoft.vwt.soa.vote.config
 * @Description: TODO
 * @author: xutf
 * @date: 2016年5月3日 上午10:32:11
 * @version: V1.0
 */
package com.royasoft.vwt.controller.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.royasoft.vwt.base.zk.ZkUtil;

/**
 * @ClassName: ZkConfig
 * @Description: 加载zk配置
 * @author: xutf
 * @date: 2016年5月3日 上午10:32:11
 */

@Configuration
public class ZkConfig {
	private static final String ADDRESS = "royasoft.zookeeper.address";

	private static final Logger logger = LoggerFactory.getLogger(ZkConfig.class);

	@Bean
	public String zkUrl() {
		return StringUtils.isNotEmpty(System.getProperty(ADDRESS)) ? System.getProperty(ADDRESS) : System.getenv(ADDRESS);
	}

	@Bean
	public ZkUtil zkUtil(String zkUrl) {
		ZkUtil zkUtil = new ZkUtil(zkUrl, "");
		logger.info("ZK客服端初始化成功，地址：{}", zkUrl);
		return zkUtil;
	}

}