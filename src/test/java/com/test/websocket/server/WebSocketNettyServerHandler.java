package com.test.websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author xh
 * @date 2024-07-25
 * @Description:
 */
@Slf4j
public class WebSocketNettyServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        // 处理websocket客户端的消息
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
            String requestContent = textFrame.text();
            log.info("收到文本消息： " + requestContent);
        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) msg;
            byte[] requestContent = new byte[binaryFrame.content().capacity()];
            binaryFrame.content().getBytes(0, requestContent);
            log.info("收到二进制消息： " + Arrays.toString(requestContent));
        }
    }
}
