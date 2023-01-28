package com.wyd.client;

import com.wyd.message.ChatResponseMessage;
import com.wyd.message.GroupChatResponseMessage;
import com.wyd.message.GroupCreateResponseMessage;
import com.wyd.message.GroupJoinResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

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
    }

}
