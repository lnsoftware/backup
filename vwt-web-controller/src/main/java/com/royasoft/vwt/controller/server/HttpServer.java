/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.server;

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

/**
 * netty 实现的http server
 * 
 * @author jxue
 * 
 */
public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    /**
     * 启动接口服务器
     * 
     */
    public static void run(final int port) throws Exception {
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
                    ch.pipeline().addLast(new HttpServerHandler());
                }
            });

            ChannelFuture f = boot.bind(port).sync(); // 绑定端口，同步等待成功

            log.info("服务启动成功，监听端口为：{}(内网)", port);

            f.channel().closeFuture().sync();
        } finally {
            log.info("释放服务资源...");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
