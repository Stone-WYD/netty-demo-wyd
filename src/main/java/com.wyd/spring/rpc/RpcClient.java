package com.wyd.spring.rpc;

import com.wyd.client.handler.RpcResponseMessageHandler;
import com.wyd.message.RpcRequestMessage;
import com.wyd.protocol.MessageCodecSharableFit;
import com.wyd.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageCodecSharableFit CODEC_HANDLER = new MessageCodecSharableFit();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();

        try {
            Channel channel = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProcotolFrameDecoder());
                            ch.pipeline().addLast(CODEC_HANDLER);
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(RPC_HANDLER);
                        }
                    })
                    .connect("localhost", 8080).sync().channel();

            channel.writeAndFlush(new RpcRequestMessage(1,
                    "com.wyd.server.service.HelloService",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"张三"}
            )).addListener(promise -> {
                if (!promise.isSuccess()) {
                    Throwable cause = promise.cause();
                    log.error("error", cause);
                }
            });
            channel.closeFuture().sync();
        }catch (Exception e){
            log.error("client error", e);
        }finally {
            group.shutdownGracefully();
        }

    }
}
