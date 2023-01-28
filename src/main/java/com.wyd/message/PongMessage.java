package com.wyd.message;

public class PongMessage extends Message {
    @Override
    public int getMessageType() {
        return com.wyd.message.Message.PongMessage;
    }
}
