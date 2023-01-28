package com.wyd.server.handler;

import com.wyd.message.ChatRequestMessage;
import com.wyd.message.ChatResponseMessage;
import com.wyd.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage chatMessage) throws Exception {

        String to = chatMessage.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to);
        // 在线
        if (channel != null ){
            channel.writeAndFlush(new ChatResponseMessage(chatMessage.getFrom(), chatMessage.getContent()) );
        }
        // 不在线
        else {
            ctx.writeAndFlush(new ChatResponseMessage(false, "对方不在线"));
        }

    }
}
