package com.wyd.server.handler;

import com.wyd.message.GroupQuitRequestMessage;
import com.wyd.message.GroupQuitResponseMessage;
import com.wyd.server.session.Group;
import com.wyd.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

public class GroupQuitRequestMessageHandler extends SimpleChannelInboundHandler<GroupQuitRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupQuitRequestMessage groupQuit) throws Exception {

        String groupName = groupQuit.getGroupName();
        String username = groupQuit.getUsername();

        Group group = GroupSessionFactory.getGroupSession().removeMember(groupName,username);
        if (group != null) {
            ctx.writeAndFlush(new GroupQuitResponseMessage(true, "退出" + groupName + "成功！"));
        }else {
            ctx.writeAndFlush(new GroupQuitResponseMessage(false, groupName + "不存在！"));
        }

    }
}
