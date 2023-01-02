package com.test.netty.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LoggingHandler;

public class HelloServer {

    public static void main(String[] args) {
        //1. 启动器，负责组装netty组件，启动服务器
        new ServerBootstrap()
                //2. BossEventLoop，WorkerEventLoop(selector,thread),group组
                .group(new NioEventLoopGroup())
                //3. 选择服务器的ServerSocketChannel实现
                .channel(NioServerSocketChannel.class)
                //4. boss负责处理连接，worker（child）负责处理读写事件
                .childHandler(
                        //5. channel 代表和客户端进行数据读写的通道 Initializer 初始化，负责添加别的 handler
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                //ch.pipeline().addLast(new LoggingHandler());
                                ch.pipeline().addLast(new StringDecoder());
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        //读事件
                                        System.out.println(msg);
                                    }
                                });
                            }
                        }
                )
                .bind(8080);
    }
}
