package com.wyd.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class GroupChatResponseMessage extends AbstractResponseMessage {

    private String group;
    private String from;
    private String content;

    public GroupChatResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    public GroupChatResponseMessage(String group, String from, String content) {
        this.group = group;
        this.from = from;
        this.content = content;
    }
    @Override
    public int getMessageType() {
        return com.wyd.message.Message.GroupChatResponseMessage;
    }
}
