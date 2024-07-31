package com.test.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TestPipeline {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("1");
                                /*ByteBuf buf = (ByteBuf) msg;
                                String name = buf.toString(Charset.defaultCharset());*/
                                String temResult = "第一个 handler 已处理";
                                super.channelRead(ctx, temResult);
                            }
                        });
                        pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                           /* @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                            }*/

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                log.info("2 第二个 handler 收到上一个 handler 传来的数据：{}", msg);
                                Student student = new Student(("wyd"));
                                super.channelRead(ctx, student);
                            }
                        });

                        pipeline.addLast(new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4");
                                super.write(ctx, msg, promise);
                            }
                        });

                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof Student) {
                                    Student student = (Student) msg;
                                    log.info("3 从上一个handler收到一个学生，姓名为：{}", student.getName());
                                }

                                /*log.debug("student: {},class: {}", ((Student) msg).getName(),msg.getClass());*/
                                //读完数据开始写
                                /*channel的writeAndFlush方法，从tail往前找写的handler
                                 channel.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes(StandardCharsets.UTF_8)));*/
                                channel.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes(StandardCharsets.UTF_8)));
                                //ctx的writeAndFlush方法，从当前read handler往前找写handler
                                // ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes(StandardCharsets.UTF_8)));

                                //super.channelRead(ctx, msg); 没有下一个读handler，可以省去
                            }
                        });

                        pipeline.addLast(new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast(new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

                                log.debug("6 " + promise);
                                super.write(ctx, msg, promise);
                            }
                        });

                    }
                })
                .bind(8080);
    }

    @Data
    @AllArgsConstructor
    static class Student {
        private String name;
    }
}
