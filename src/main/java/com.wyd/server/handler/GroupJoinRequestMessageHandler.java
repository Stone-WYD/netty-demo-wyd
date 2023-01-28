package com.wyd.server.handler;

import com.wyd.message.GroupJoinRequestMessage;
import com.wyd.message.GroupJoinResponseMessage;
import com.wyd.server.session.Group;
import com.wyd.server.session.GroupSession;
import com.wyd.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;
@ChannelHandler.Sharable
public class GroupJoinRequestMessageHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage groupJoin) throws Exception {
        String groupName = groupJoin.getGroupName();
        String username = groupJoin.getUsername();// 加入群中的人
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        // 将人加入群中
        Group group = groupSession.joinMember(groupName, username);

        if (group != null){
            ctx.writeAndFlush( new GroupJoinResponseMessage(true,"成功加入" + groupName + "!"));
            // 通知其他群成员有人进群
            groupSession.getMembersChannel(groupName).forEach(channel -> {
                        if (channel != ctx.channel()){
                            channel.writeAndFlush(new GroupJoinResponseMessage(true, username + "加入" + groupName));
                        }else channel.writeAndFlush(new GroupJoinResponseMessage(true,  "入群成功！"));
                    }
            );
        }else ctx.writeAndFlush(new GroupJoinResponseMessage(false, groupName + "不存在！"));

    }
}
