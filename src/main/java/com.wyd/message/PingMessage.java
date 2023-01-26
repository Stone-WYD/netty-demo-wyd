package com.wyd.message;

public class PingMessage extends Message {
    @Override
    public int getMessageType() {
        return com.wyd.message.PingMessage;
    }
}
