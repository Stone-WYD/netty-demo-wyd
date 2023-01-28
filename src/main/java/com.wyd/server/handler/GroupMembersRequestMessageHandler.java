package com.wyd.server.handler;

import com.wyd.message.GroupMembersRequestMessage;
import com.wyd.message.GroupMembersResponseMessage;
import com.wyd.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

public class GroupMembersRequestMessageHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage groupMember) throws Exception {
        Set<String> members = GroupSessionFactory.getGroupSession().getMembers(groupMember.getGroupName());
        ctx.writeAndFlush(new GroupMembersResponseMessage(members));
    }
}
