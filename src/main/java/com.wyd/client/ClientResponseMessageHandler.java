package com.wyd.client;

import com.wyd.message.*;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class ClientResponseMessageHandler {

    public static void clientResponseMessageHandler(ChannelHandlerContext ctx, Object msg){
        // 收到单聊消息
        if (msg instanceof ChatResponseMessage) {
            ChatResponseMessage chatResponse = (ChatResponseMessage) msg;
            if (chatResponse.isSuccess()){
                System.out.println("收到来自 " + chatResponse.getFrom() + " 的单聊消息:");
                System.out.println(chatResponse.getContent());
            }else log.error(chatResponse.getReason());
        }
        // 收到被拉入群的消息
        if (msg instanceof GroupCreateResponseMessage) {
            GroupCreateResponseMessage createGroup = (GroupCreateResponseMessage) msg;
            if (createGroup.isSuccess()){
                System.out.println(createGroup.getReason());
            }
        }
        // 收到有人入群的消息
        if (msg instanceof GroupJoinResponseMessage) {
            GroupJoinResponseMessage joinGroup = (GroupJoinResponseMessage) msg;
            if (joinGroup.isSuccess()){
                System.out.println(joinGroup.getReason());
            }
        }
        // 收到群聊消息
        if (msg instanceof GroupChatResponseMessage) {
            GroupChatResponseMessage groupChatResponse = (GroupChatResponseMessage) msg;
            if (groupChatResponse.isSuccess()) {
                System.out.println("收到来自群 " + groupChatResponse.getGroup() + "中群成员" + groupChatResponse.getFrom()  + " 的群聊消息:");
                System.out.println(groupChatResponse.getContent());
            }else log.error(groupChatResponse.getReason());
        }
        // 收到获取群成员消息
        if (msg instanceof GroupMembersResponseMessage) {
            GroupMembersResponseMessage membersReponse = (GroupMembersResponseMessage) msg;
            if (membersReponse.getMembers() != null) {
                StringBuilder sb = new StringBuilder("群成员为：");
                membersReponse.getMembers().stream().forEach(s -> sb.append(s).append(","));
                sb.replace(sb.length()-1,sb.length(),"");
                System.out.println(sb.toString());
            }


        }
    }

}
