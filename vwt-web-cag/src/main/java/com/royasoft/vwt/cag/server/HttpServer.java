/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.royasoft.vwt.cag.conf.ParamConfig;
import com.royasoft.vwt.cag.listener.BaseInfoLoadListener;
import com.royasoft.vwt.cag.listener.LogbackLoadListener;
import com.royasoft.vwt.cag.listener.RocketMqLoadListener;
import com.royasoft.vwt.cag.queue.ThreadPoolManage;

/**
 * netty 实现的http server
 * 
 * @author jxue
 * 
 */
@SpringBootApplication
@ComponentScan("com.royasoft.vwt.cag")
public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public static ConfigurableApplicationContext context = null;

    /**
     * http server 启动方法
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        try {
            new HttpServer().run();
        } catch (Exception e) {
            logger.error("cag服务启动异常...", e);
        }
    }
    
    

    /**
     * cag server 启动
     * 
     * @throws Exception
     */
    private void run() throws Exception {
        logger.info("cag server 开始启动...");
        SpringApplication app = new SpringApplication(HttpServer.class);
        app.addListeners(new LogbackLoadListener());
        app.addListeners(new BaseInfoLoadListener());
        app.addListeners(new RocketMqLoadListener());
        context = app.run();

        shutdownHook();

        startServer();
    }

    /**
     * 启动接口服务器
     * 
     * @throws InterruptedException
     */
    private void startServer() throws InterruptedException {
        new ThreadPoolManage().initThreadPool();
        EventLoopGroup bossGroup = new NioEventLoopGroup(64);
        EventLoopGroup workerGroup = new NioEventLoopGroup(64);
        try {
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(bossGroup, workerGroup);
            boot.channel(NioServerSocketChannel.class);
            boot.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {

                    ch.pipeline().addLast(new HttpRequestDecoder());
                    ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 50));
                    ch.pipeline().addLast(new HttpResponseEncoder());
                    ch.pipeline().addLast(new ChunkedWriteHandler());
                    ch.pipeline().addLast(new HttpServerHandler());// HttpServer.context.getBean(HttpServerHandler.class));
                }
            });

            ChannelFuture f = boot.bind(Integer.valueOf(ParamConfig.cag_port)).sync(); // 绑定端口，同步等待成功

            logger.info("cag注册至zk成功,监听端口为{}(内网)", ParamConfig.cag_port);

            f.channel().closeFuture().sync();
        } finally {// 释放线程池资源
            logger.info("释放cag服务资源..");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 增加服务关闭事件
     */
    private void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                logger.info("cag Server 停止中...");
                HttpServer.context.close(); // 关闭spring容器
                logger.info("cag Server 已停止...");
            }
        }));
    }

}
