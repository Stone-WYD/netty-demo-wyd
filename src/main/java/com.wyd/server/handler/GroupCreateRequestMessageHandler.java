package com.wyd.server.handler;

import com.wyd.message.GroupChatResponseMessage;
import com.wyd.message.GroupCreateRequestMessage;
import com.wyd.message.GroupCreateResponseMessage;
import com.wyd.server.session.Group;
import com.wyd.server.session.GroupSession;
import com.wyd.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage message) throws Exception {
        String groupName = message.getGroupName();
        Set<String> members = message.getMembers();

        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);
        if (group == null){
            // 创建成功，发送给创建者信息
            ctx.writeAndFlush(new GroupCreateResponseMessage(true,   groupName + "创建成功！" ));
            // 给群成员发送消息
            groupSession.getMembersChannel(groupName).stream().forEach(
                    channel -> channel.writeAndFlush(
                            new GroupCreateResponseMessage(true, "您已被拉入" + groupName)
                    ));
        }else
            ctx.writeAndFlush(new GroupCreateResponseMessage( false, "群名已存在，创建失败！"));
    }
}
