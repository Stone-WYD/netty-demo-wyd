package com.test.websocket.client;

import com.test.util.ConfigPropertiesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author xh
 * @date 2024-07-24
 * @Description: WebSocket 连接客户端：获取当前节目使用
 */
@Slf4j
public class WebsocketNettyClient {

    public static void main(String[] args) throws Exception {
        // 获取一个 websocket 连接
        Channel dest = WebsocketNettyClient.dest();
        // fixme 这里连接虽然建立了，但 ssl 层还没有处理好，如果接着就发送消息会出现问题，演示用所以直接睡眠了，此处应
        //  用 synchronized 等方式阻塞等待 ssl 处理好
        Thread.sleep(3000);
        // 向连接中发送信息到服务端（如果一段时间不发送，则会触发写空闲事件，发送心跳包）
        send(dest, "测试一下连接");
        // 关闭连接
        dest.close();
        // 关闭客户端，这里关闭后是无法再获取连接的
        closeClient();
    }


    public static Channel dest() throws Exception {
        ChannelFuture cf = boot.connect(webSocketURL.getHost(), webSocketURL.getPort()).sync();
        return cf.channel();
    }

    public static void send(Channel channel, String textMsg) {

        if (channel != null && channel.isActive()) {
            TextWebSocketFrame frame = new TextWebSocketFrame(textMsg);
            channel.writeAndFlush(frame).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isDone() && channelFuture.isSuccess()) {
                    log.info("     ================= 发送成功.");
                } else {
                    channelFuture.channel().close();
                    log.info("     ================= 发送失败. cause = " + channelFuture.cause());
                    channelFuture.cause().printStackTrace();
                }
            });
        } else {
            log.error("消息发送失败！ textMsg = " + textMsg);
        }
    }

    public static void closeClient() {
        group.shutdownGracefully();
    }

    private static URI webSocketURL;
    private static Bootstrap boot;
    //  一般来说是没有关闭 group 的处理，group 本质上是个线程池，只要有相关功能还需要用，就不应该关闭。如果碰到一些不可控因素如服务器崩溃，
    //  那么应该靠重启而不是通过代码进行容错。当然，如果有控制管理面板，也可以设置相应开关功能。
    private static EventLoopGroup group = new NioEventLoopGroup(2);

    static {
        try {
            // 根据配置进行一些初始化
            if ("true".equals(ConfigPropertiesUtil.getProperty("netty.websocket.enable"))) {
                String webSocketUri = ConfigPropertiesUtil.getProperty("netty.websocket.uri");
                webSocketURL = new URI(webSocketUri);

                // ssl 配置
                SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
                // 直接信任签证书
                sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                SslContext sslContext = sslContextBuilder.build();

                boot = new Bootstrap().option(ChannelOption.SO_KEEPALIVE, true)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel sc) throws Exception {
                                ChannelPipeline pipeline = sc.pipeline();
                                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                                // Http 处理前加上 ssl 处理 handler
                                pipeline.addLast("ssl", sslContext.newHandler(sc.alloc()));
                                // Http 客户端编解码器。解码时，会根据换行符（CRLF）得到 Line、Header 和 Body对应的对象
                                pipeline.addLast(new HttpClientCodec());
                                /* 当请求体（Body）传输数据很多时，请求头中会有 Transfer-Encoding: chunked 的参数而不再是 Content-Length，然后 ChunkedWriteHandler
                                * 不会激活后续处理流程，而是开启分块处理程序来处理后续的 truncked 包直到数据传输完成。 */
                                pipeline.addLast(new ChunkedWriteHandler());
                                // Http 对象聚合器：把 Line、Header 和 Body对应的对象转换为一个单一的完全FullHttpRequest或是FullHttpResponse，
                                pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                                // websocket 客户端协议处理程序
                                pipeline.addLast(new WebSocketClientProtocolHandler(WebSocketClientHandshakerFactory
                                        .newHandshaker(webSocketURL, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
                                // 如果一段时间（单位：秒）没有向服务器写数据，就会触发一个 IdleState#WRITER_IDLE 事件
                                pipeline.addLast(new IdleStateHandler(0, 1, 0));
                                // todo 测试问题：netty 中关于 handler 的多线程情况分析
                                // pipeline.addLast(new WebsocketNettyClientOutHandler());
                                // 业务处理
                                pipeline.addLast(new WebsocketNettyClientInboundHandler());

                            }
                        });
            }
        } catch (URISyntaxException | SSLException e) {
            throw new RuntimeException(e);
        }
    }



}
