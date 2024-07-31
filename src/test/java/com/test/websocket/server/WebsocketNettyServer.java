package com.test.websocket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * @author xh
 * @date 2024-07-25
 * @Description:
 */
@Slf4j
public class WebsocketNettyServer {

    private void init() {
        log.info("正在启动websocket服务器");
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup();
        try {

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, work);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    // 加入 ssl 层处理类，这里生成的 sslEngine ，不能共享
                    SSLEngine sslEngine = getServerSslContext().createSSLEngine();
                    sslEngine.setNeedClientAuth(false);
                    sslEngine.setUseClientMode(false);
                    pipeline.addLast(new SslHandler(sslEngine));

                    // 基于http协议的长连接 需要使用http协议的解码 编码器
                    pipeline.addLast(new HttpServerCodec());
                    // 以块的方式处理
                    pipeline.addLast(new ChunkedWriteHandler());
                    // 把多个消息转换为一个单一的完全FullHttpRequest或是FullHttpResponse，
                    // 原因是HTTP解码器会在解析每个HTTP消息中生成多个消息对象
                    pipeline.addLast(new HttpObjectAggregator(8192));
                    /**
                     * 对于websocket是以frame的形式传递
                     * WebSocketFrame
                     *  浏览器 ws://localhost:7070/ 不在是http协议
                     *  WebSocketServerProtocolHandler 将http协议升级为ws协议 即保持长链接
                     */
                    pipeline.addLast(new WebSocketServerProtocolHandler("/helloWs"));
                    pipeline.addLast("handler", new WebSocketNettyServerHandler());// 自定义的业务handler,这里处理WebSocket建链请求和消息发送请求
                }
            });
            // 这里获取到的 channel 是 NioServerSocketChannel，用于监听端口号，和 childHandler 中的 channel 不一样
            Channel channel = bootstrap.bind(7070).sync().channel();
            log.info("webSocket服务器启动成功：" + channel);
            channel.closeFuture().sync();
            log.info("连接关闭了，服务端运行到这了。。。");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("运行出错：" + e);
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
            log.info("websocket服务器已关闭");
        }
    }

    private SSLContext getServerSslContext() throws Exception {

        InputStream inputStream = Files.newInputStream(Paths.get("mystore.jks"));
        String sslPassword = "1234567";

        log.info("加载了密码: {}", sslPassword);

        char[] passArray = sslPassword.toCharArray();
        SSLContext sslContext = SSLContext.getInstance("SSLv3");
        KeyStore ks = KeyStore.getInstance("JKS");
        //加载 keytool 生成的文件
        ks.load(inputStream, passArray);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, passArray);
        sslContext.init(kmf.getKeyManagers(), null, null);
        inputStream.close();
        return sslContext;
    }



    public static void main(String[] args) {
        new WebsocketNettyServer().init();
    }


}
