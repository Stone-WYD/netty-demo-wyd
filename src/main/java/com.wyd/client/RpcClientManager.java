package com.wyd.client;

import com.wyd.client.handler.RpcResponseMessageHandler;
import com.wyd.message.RpcRequestMessage;
import com.wyd.protocol.MessageCodecSharableFit;
import com.wyd.protocol.ProcotolFrameDecoder;
import com.wyd.protocol.SequenceIdGenerator;
import com.wyd.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

@Slf4j
public class RpcClientManager {

    // 调用代理类方法
    public static void main(String[] args) {
        HelloService proxyService = getProxyService(HelloService.class);
        System.out.println(proxyService.sayHello("yxy"));
    }

    // 使用动态代理包装rpc
    public static <T> T getProxyService(Class<T> serviceClass){
        Object o = Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                ((proxy, method, args) -> {
                    RpcRequestMessage msg = new RpcRequestMessage(
                            SequenceIdGenerator.nextId(),
                            serviceClass.getName(),
                            method.getName(),
                            method.getReturnType(),
                            method.getParameterTypes(),
                            args
                            );
                    Channel channel = getChannel();
                    channel.writeAndFlush(msg);
                    // 新建一个promise对象，用于和netty线程交互获取返回结果
                    Promise<Object> promise = new DefaultPromise<>(channel.eventLoop());
                    RpcResponseMessageHandler.PROMISES.put(msg.getSequenceId(), promise);

                    // 等待netty线程返回结果
                    promise.await();
                    if (promise.isSuccess()){
                        return promise.getNow();
                    }else {
                        // 调用失败
                        throw new RuntimeException(promise.cause());
                    }
                }));
        return (T) o;
    }

    // 获取 channel
    private static Channel channel = null;
    private static final Object LOCK = new Object();
    public static Channel getChannel(){
        if (channel != null){
            return channel;
        }else {
            synchronized (LOCK){
                if (channel != null){
                    return channel;
                }
                initChannel();
                return channel;
            }
        }
    }

    // 初始化 channel
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageCodecSharableFit CODEC_HANDLER = new MessageCodecSharableFit();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap()
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
                });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}
