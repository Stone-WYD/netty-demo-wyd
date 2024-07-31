package com.test.websocket.client;

import com.test.util.ConfigPropertiesUtil;
import com.test.util.SystemUtil;
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
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

/**
 * @author xh
 * @date 2024-07-24
 * @Description: WebSocket 连接客户端：获取当前节目使用
 */
@Slf4j
public class WebsocketNettyClient {

    public static void main(String[] args) throws Exception {
        WebsocketNettyClient client = new WebsocketNettyClient();
        client.test();
    }


    public void test() throws Exception {
        Channel dest = dest();

        // closeFuture 方法收到关闭指令后返回 future，关闭后添加一些操作。这里加入的 CLOSE(因为其作用是关闭channel) 是没有意义的
        // dest.closeFuture().addListener((ChannelFutureListener.CLOSE)).sync();
        dest.closeFuture().addListener(future -> {
            log.info("执行到这里了");
            group.shutdownGracefully();
            // todo 为何运行到此处后如果不强制关闭程序，程序不会自行结束？
            // System.exit(1);
            SystemUtil.showThreadsInfo();
            Thread.sleep(1000);
            SystemUtil.showThreadsInfo();
        });
    }

    private EventLoopGroup group = new NioEventLoopGroup();


    public static void send(Channel channel) {
        final String textMsg = "握手完成后直接发送的消息";

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



    private static URI webSocketURL;
    private static Bootstrap boot;

    static {
        try {
            // 根据配置进行一些初始化
            if ("true".equals(ConfigPropertiesUtil.getProperty("netty.websocket.enable"))) {
                String webSocketUri = ConfigPropertiesUtil.getProperty("netty.websocket.uri");
                webSocketURL = new URI(webSocketUri);
                // fixme 没有关闭 group 的处理，group 本质上是个线程池，只要有相关功能还需要用，就不应该关闭。如果碰到一些不可控因素如服务器崩溃，
                //  那么应该靠重启而不是通过代码进行容错。当然，如果有控制管理面板，也可以设置相应开关功能。
                EventLoopGroup group = new NioEventLoopGroup();

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
                                // 业务处理
                                pipeline.addLast(new WebsocketNettyClientHandler());
                            }
                        });
            }
        } catch (URISyntaxException | SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Channel dest() throws Exception {
        ChannelFuture cf = boot.connect(webSocketURL.getHost(), webSocketURL.getPort()).sync();
        return cf.channel();
    }

}
