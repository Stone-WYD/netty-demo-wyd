package com.wyd.server.handler;

import com.wyd.message.GroupChatRequestMessage;
import com.wyd.message.GroupChatResponseMessage;
import com.wyd.server.session.GroupSession;
import com.wyd.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage groupChat) throws Exception {

        String groupName = groupChat.getGroupName();
        String from = groupChat.getFrom();
        String content = groupChat.getContent();

        GroupSession groupSession = GroupSessionFactory.getGroupSession();

        groupSession.getMembersChannel(groupName).stream().forEach(channel ->
                channel.writeAndFlush(new GroupChatResponseMessage(groupName,from,content)));

    }
}
