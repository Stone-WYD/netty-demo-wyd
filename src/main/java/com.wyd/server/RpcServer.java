package com.wyd.server;

import com.wyd.protocol.MessageCodecSharableFit;
import com.wyd.protocol.ProcotolFrameDecoder;
import com.wyd.server.handler.RpcRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServer {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        MessageCodecSharableFit CODEC = new MessageCodecSharableFit();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();
        try {
            Channel channel = new ServerBootstrap()
                    .channel(NioServerSocketChannel.class)
                    .group(boss, worker)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProcotolFrameDecoder());
                            ch.pipeline().addLast(CODEC);
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(RPC_HANDLER);
                        }
                    }).bind(8080).sync().channel();
            channel.closeFuture().sync();
        }catch (Exception e){
            log.error("服务器错误: ",e);
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
