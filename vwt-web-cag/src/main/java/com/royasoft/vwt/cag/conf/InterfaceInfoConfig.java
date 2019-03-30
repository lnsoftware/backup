/*
 * Copyright © 2002-2015 上海若雅软件系统有限公司
 * 
 * @Title: LogbackLoadListener.java
 * 
 * @Prject: vwt-base-services
 * 
 * @Package: com.royasoft.vwt.soa.base
 * 
 * @Description: TODO
 * 
 * @author: xutf
 * 
 * @date: 2016年5月5日 下午3:28:01
 * 
 * @version: V1.0
 */

package com.royasoft.vwt.cag.conf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.royasoft.vwt.base.zk.ZkUtil;
import com.royasoft.vwt.cag.constant.ZkNodeConstant;
import com.royasoft.vwt.cag.vo.InterfaceVo;

/**
 * 加载zk中接口信息
 * 
 * @author MB
 *
 */
@Configuration
public class InterfaceInfoConfig {

  private static final Logger logger = LoggerFactory.getLogger(InterfaceInfoConfig.class);

  @Resource
  ZkUtil zkUtil;

  @PostConstruct
  public void initInterfaceInfo() {
    try {
      List<String> allInterfaceUrlZkNodeList = zkUtil.findChildren(ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_URL_INFO);
      List<String> allInterfaceZkNodeList = zkUtil.findChildren(ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_INFO);
      loadInterfaceInfo(allInterfaceUrlZkNodeList, allInterfaceZkNodeList);

      // 配置zk监听
      zkUtil.addChildWatcher(ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_URL_INFO, new ZkUtil.ZkChildOp() {
        @Override
        public void process(PathChildrenCache arg0, PathChildrenCacheEvent arg1) {
          try {
            List<String> allInterfaceUrlZkNodeList = zkUtil.findChildren(ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_URL_INFO);

            List<String> allInterfaceZkNodeList = zkUtil.findChildren(ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_INFO);
            loadInterfaceInfo(allInterfaceUrlZkNodeList, allInterfaceZkNodeList);
          } catch (Exception e) {
            logger.error("加载zk中接口信息异常", e);
          }
        }
      });

      // 配置zk监听
      zkUtil.addChildWatcher(ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_INFO, new ZkUtil.ZkChildOp() {
        @Override
        public void process(PathChildrenCache arg0, PathChildrenCacheEvent arg1) {
          try {
            List<String> allInterfaceUrlZkNodeList = zkUtil.findChildren(ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_URL_INFO);

            List<String> allInterfaceZkNodeList = zkUtil.findChildren(ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_INFO);
            loadInterfaceInfo(allInterfaceUrlZkNodeList, allInterfaceZkNodeList);
          } catch (Exception e) {
            logger.error("加载zk中接口信息异常", e);
          }
        }
      });
    } catch (Exception e) {
      logger.error("加载zk中接口信息异常", e);
    }
  }

  private void loadInterfaceInfo(List<String> allInterfaceUrlZkNodeList, List<String> allInterfaceZkNodeList) {
    if (CollectionUtils.isEmpty(allInterfaceZkNodeList) || CollectionUtils.isEmpty(allInterfaceUrlZkNodeList)) {
      return;
    }
    ParamConfig.interfaceVoMap.clear();

    Map<String, String> urlMap = new HashMap<String, String>();

    for (String nodePath : allInterfaceUrlZkNodeList) {
      try {
        String nodePathAll = ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_URL_INFO + "/" + nodePath;
        String nodeValue = zkUtil.findData(nodePathAll);
        if (StringUtils.isEmpty(nodeValue)) {
          continue;
        }

        urlMap.put(nodePath, nodeValue);
      } catch (Exception e) {
        logger.error("初始化接口信息失败...{}", nodePath, e);
      }
    }

    if (MapUtils.isEmpty(urlMap)) {
      return;
    }

    int succCount = 0;
    for (String nodePath : allInterfaceZkNodeList) {
      try {
        String nodePathAll = ZkNodeConstant.InterfaceInfo.ZK_CAG_INTERFACE_INFO + "/" + nodePath;
        String nodeValue = zkUtil.findData(nodePathAll);
        JSONObject interfaceJson = JSON.parseObject(nodeValue);
        String functionId = interfaceJson.getString("functionId");
        String encodeType = interfaceJson.getString("encodeType");
        String requestUrl = interfaceJson.getString("requestUrl");
        String urlId = interfaceJson.getString("urlId");
        if (StringUtils.isEmpty(requestUrl) || StringUtils.isEmpty(encodeType) || StringUtils.isEmpty(functionId) || StringUtils.isEmpty(urlId)) {
          continue;
        }
        String cloudUrl = urlMap.get(urlId);
        if (StringUtils.isEmpty(cloudUrl)) {
          continue;
        }

        ParamConfig.interfaceVoMap.put(functionId, new InterfaceVo(functionId, encodeType, cloudUrl + requestUrl));
        succCount++;
      } catch (Exception e) {
        logger.error("初始化接口信息失败...{}", nodePath, e);
      }
    }

    logger.info("加载接口配置信息结束....加载成功:{}....加载失败:{}......interfaceVoMap:{}", succCount, allInterfaceZkNodeList.size() - succCount,
        JSON.toJSONString(ParamConfig.interfaceVoMap));
  }
}
