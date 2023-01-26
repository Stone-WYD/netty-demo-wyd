package com.test.advance.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;

public class Client2 {

    static final Logger log = LoggerFactory.getLogger(Client2.class);

    public static void main(String[] args) throws InterruptedException {
        send();
        System.out.println("finish.");
    }

    public static byte[] fill10Bytes(char c,int len){
        byte[] bytes = new byte[10];
        Arrays.fill(bytes,(byte) '-');
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) c;
        }
        System.out.println(new String(bytes));
        return bytes;
    }

    private static void send() throws InterruptedException {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .channel(NioSocketChannel.class)
                    .group(worker)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ByteBuf buf = ctx.alloc().buffer();
                                    char c = '0';
                                    Random r = new Random();
                                    //定长
                                    for (int i = 0; i < 10; i++) {
                                        byte[] bytes = fill10Bytes(c, r.nextInt(10) + 1);
                                        c++;
                                        buf.writeBytes(bytes);
                                    }

                                    ctx.writeAndFlush(buf);
                                }
                            });
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("client error" , e);
        }finally {
            worker.shutdownGracefully();
        }

    }

}
