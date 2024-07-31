package com.test.websocket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xh
 * @date 2024-07-31
 * @Description: 写消息相关处理
 */
@Slf4j
public class WebsocketNettyClientOutHandler extends ChannelOutboundHandlerAdapter {

    private volatile boolean connectFlag = false;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Boolean) {
            connectFlag = true;
            synchronized (this) {
                notifyAll();
                log.info("释放等待中的线程...");
            }
            return;
        }
        if (msg instanceof TextWebSocketFrame) {
            if (!connectFlag) {
                synchronized (this) {
                    log.info("连接没建立完成，进入等待...");
                    // 挂在 handler 上等待连接建立（ssl 层功能完成）。只有一个线程会完成解锁操作，所以此处不必双重判断
                    wait();
                    log.info("连接建立完成，往下执行...");
                }
            }
            super.write(ctx, msg, promise);
            return;
        }

        super.write(ctx, msg, promise);
    }
}
