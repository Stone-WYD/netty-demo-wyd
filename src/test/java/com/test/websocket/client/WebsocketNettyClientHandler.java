package com.test.websocket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xh
 * @date 2024-07-25
 * @Description:
 */
@Slf4j
public class WebsocketNettyClientHandler  extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private int times = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {


        if (WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE
                .equals(evt)) {
            // websocket 连接建立完成
            log.info(ctx.channel().id().asShortText() + " websocket连接握手完成！");
        }

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                times++;
                if (times > 5) {
                    // 五次之后自动断开连接
                    ctx.close();
                } else {
                    // 写空闲事件触发
                    ctx.writeAndFlush(new TextWebSocketFrame("ping"));
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 连接断开时
        log.info("netty websocket 连接断开了...");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 发生异常时断开连接，记录异常日志，且阻止异常进一步扩散
        log.error("CurrentProgramNettyClientHandler 调用时出现异常：{}", cause.getMessage());
        ctx.close();
    }

}
