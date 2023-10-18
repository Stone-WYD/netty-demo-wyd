package com.test.netty.wyd;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @program: netty-demo-wyd
 * @description:
 * @author: Stone
 * @create: 2023-10-18 11:10
 **/
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("client received data from server: " + msg);
    }
}
