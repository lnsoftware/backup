/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.soa.sundry;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.royasoft.vwt.soa.base.zookeeper.api.interfaces.ZookeeperInterface;

/**
 * 系统启动时，初始化dubbo服务配置信息
 * 
 * @author yujun
 */
public class InitBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(InitBeanPostProcessor.class);

    /** zookeeper集群地址 **/
    private String zkUrl = "";

    /** dubbo服务端口起始位置 **/
    private int dubbo_port_start = 30000;

    @Autowired
    private ZookeeperInterface zookeeperInterface;

    /**
     * 启动初始化bean
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        // ////// 初始化service前，先注册dubbo服务///////////////
        if (bean instanceof ApplicationConfig) {
            ApplicationConfig applicationConfig = (ApplicationConfig) bean;
            logger.info("{}启动中...", applicationConfig.getName());
        }

        if (bean instanceof ProtocolConfig) {
            ProtocolConfig protocolConfig = (ProtocolConfig) bean;
            protocolConfig.setPort(getNotUsePort());
            logger.info("端口号：{}", protocolConfig.getPort());
        }

        if (bean instanceof RegistryConfig) {
            RegistryConfig registryConfig = (RegistryConfig) bean;
            registryConfig.setProtocol("zookeeper");
            registryConfig.setAddress(zkUrl);
            registryConfig.setCheck(true);
            registryConfig.setTimeout(60000);
            logger.info("zookeeperd地址：{}", registryConfig.getAddress());
        }

        if (bean instanceof DruidDataSource) {// 初始化datasource连接池,由于此时zookeeper service还未初始化，自行连接zk读取(后续若有解决方案需修改此处) by jxue
            logger.info("连接zookeeper：{},获取数据源配置信息", zkUrl);
            DruidDataSource datasource = (DruidDataSource) bean;
            try {

                datasource.setUrl(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/url"));
                datasource.setUsername(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/username"));
                datasource.setPassword(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/password"));
                datasource.setDriverClassName(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/driver"));

                datasource.setInitialSize(Integer.parseInt(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/initialSize")));
                datasource.setMinIdle(Integer.parseInt(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/minIdle")));
                datasource.setMaxActive(Integer.parseInt(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/maxActive")));
                datasource.setMaxWait(Long.parseLong(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/maxWait")));
                datasource.setTestWhileIdle(Boolean.parseBoolean(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/testWhileIdle")));
                datasource.setValidationQuery(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/validationQuery"));
                datasource.setFilters(zookeeperInterface.getPropertiesByNodePath(Constants.ZK_JDBC_HOME + "/filters"));

                logger.info("数据库url:{}", datasource.getUrl());
                logger.info("数据库username:{}", datasource.getUsername());
                if (datasource.getPassword().length() > 2)
                    logger.info("数据库password(只显示前两位):{}", datasource.getPassword().substring(0, 2) + "********");
                else
                    logger.info("数据库password:{}", datasource.getPassword());
                logger.info("数据库driver:{}", datasource.getDriverClassName());
                logger.info("数据库初始化连接数initialSize:{}", datasource.getInitialSize());
                logger.info("数据库最小连接数minIdle:{}", datasource.getMinIdle());
                logger.info("数据库最大连接数maxActive:{}", datasource.getMaxActive());
                logger.info("数据库获取连接等待超时的时间maxWait:{}", datasource.getMaxWait());
                logger.info("数据库testWhileIdle:{}", datasource.isTestWhileIdle());
                logger.info("数据库validationQuery:{}", datasource.getValidationQuery());
            } catch (Exception e) {
                logger.error("初始化bean datasource DruidDataSource异常", e);
                System.exit(0);
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 设置zk url
     * 
     * @param zkUrl
     */
    public void setZkUrl(String zkUrl) {
        this.zkUrl = zkUrl;
    }

    /**
     * 从起始端口号开始，逐一判断端口是否被占用，步长1
     * 
     * @return 未占用端口号
     */
    private int getNotUsePort() {
        while (true) {
            dubbo_port_start++;
            if (!isPortUsing(dubbo_port_start))
                return dubbo_port_start;
        }
    }

    /**
     * 判断端口号是否被占用
     * 
     * @param port
     * @return
     */
    private boolean isPortUsing(int port) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", port);
            return true;
        } catch (IOException e) {// 错误忽略
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

}
