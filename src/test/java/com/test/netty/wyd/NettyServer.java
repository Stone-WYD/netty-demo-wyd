package com.test.netty.wyd;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * @program: netty-demo-wyd
 * @description: netty server 学习 Rpc原理和实现时创建
 * @author: Stone
 * @create: 2023-10-17 18:19
 **/
public class NettyServer {

    private static String IP = "127.0.0.1";
    private static int port = 3333;
    private static final EventLoopGroup bossGroup =
            new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    private static final EventLoopGroup workerGroup =
            new NioEventLoopGroup(100);

    public static void init() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        // 绑定 NioServerSocketChannel 类型的 channel。
        // 对于服务器端的 nioServerSocketChannel，监听接口建立连接，创建新的 channel 绑定到 eventLoop 的操作都是一样的，所以这些操作都被 netty 封装在了内部。
        // 因此对于使用 netty 的开发者而言，唯一需要关注的内容是当 eventLoop 发现不同事件时，不同的事件处理器 ChannelHandler 应该有怎样的操作。
        bootstrap.channel(NioServerSocketChannel.class);

        // 当新的 channel 被创建了，会给它加入 childHandler 中的 channelHandler
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                // channel 中的 handler 需要使用 pipeline 来管理
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                pipeline.addLast(new NettyServerHandler());
            }
        });

        // 准备完成，开始绑定监听端口，启动服务端
        ChannelFuture channelFuture = bootstrap.bind(IP, port).sync();
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> System.out.println("Complete connection."));
        channelFuture.channel().closeFuture().sync();
    }

    public static void main(String[] args) throws Exception {
        NettyServer.init();
    }

}
