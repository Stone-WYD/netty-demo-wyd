package com.wyd.server.handler;

import com.wyd.message.GroupJoinRequestMessage;
import com.wyd.message.GroupJoinResponseMessage;
import com.wyd.server.session.GroupSession;
import com.wyd.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

public class GroupJoinRequestMessageHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage groupJoin) throws Exception {
        String groupName = groupJoin.getGroupName();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();

        Set<String> members = groupSession.getMembers(groupName);
        if (members != null){
            String username = groupJoin.getUsername(); // 加入群中的人
            members.add(username);
            ctx.writeAndFlush( new GroupJoinResponseMessage(true,"成功加入" + groupName + "!"));
            // 通知其他群成员有人进群
            groupSession.getMembersChannel(groupName).forEach(channel ->
                    channel.writeAndFlush(new GroupJoinResponseMessage( true, username + "加入" + groupName  ))
                    );
        }else ctx.writeAndFlush(new GroupJoinResponseMessage(false, groupName + "不存在！"));

    }
}
