package com.wyd.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class ChatResponseMessage extends AbstractResponseMessage {

    private String from;
    private String content;

    public ChatResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    public ChatResponseMessage(String from, String content) {
        setSuccess(true);
        this.from = from;
        this.content = content;
    }

    @Override
    public int getMessageType() {
        return com.wyd.message.Message.ChatResponseMessage;
    }
}
