package com.test.netty.c5;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EchoServer {

    public static void main(String[] args) {

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup(2);
        new ServerBootstrap()
                .group(boss,work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        /*ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

                            }
                        });*/

                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                                /* 等待2s后进行操作
                                ctx.executor().schedule(() -> {
                                    ByteBuf buf = (ByteBuf) msg;
                                    ByteBuf buffer = ctx.alloc().buffer();
                                    log.debug(((ByteBuf) msg).toString(Charset.defaultCharset()));
                                    buffer.writeBytes(buf);
                                    ctx.writeAndFlush(buffer);
                                    buffer.release();
                                }, 2, TimeUnit.SECONDS);*/

                                // 异步执行操作，当前任务流程不会中断（相当于执行流程中间又加入一个handler，这样写其实意义不大）
                                ctx.executor().execute(() ->{
                                    try {
                                        Thread.sleep(1000 * 5);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    log.info("异步执行开始。。。");
                                    ByteBuf buf = (ByteBuf) msg;
                                    ByteBuf buffer = ctx.alloc().buffer();
                                    log.debug(((ByteBuf) msg).toString(Charset.defaultCharset()));
                                    buffer.writeBytes(buf);
                                    ctx.writeAndFlush(buffer);
                                    buffer.release();
                                });
                            }
                        });
                    }
                }).bind(8080);
    }


}
