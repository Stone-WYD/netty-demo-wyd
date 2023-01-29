package com.test.netty.c5;

import com.wyd.config.Config;
import com.wyd.message.LoginRequestMessage;
import com.wyd.message.Message;
import com.wyd.protocol.MessageCodecSharableFit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;

public class TestSerializer {

    public static void main(String[] args) {
        MessageCodecSharableFit codec = new MessageCodecSharableFit();
        LoggingHandler loggingHandler = new LoggingHandler();

        EmbeddedChannel channel = new EmbeddedChannel(loggingHandler, codec, loggingHandler);
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        // 测试序列化: 对象 -> bytes[] ，出站
        channel.writeOutbound(message);

        // 测试序列化: bytes[] -> 对象 ，入站
/*        ByteBuf inData = getInData(message);
        channel.writeInbound(inData);*/
    }

    private static ByteBuf getInData(Message message){
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        // 1. 4字节 魔数
        out.writeBytes(new byte[]{1,2,3,4});
        // 2. 1字节 版本数
        out.writeByte(1);
        // 3. 1字节 序列化方式 jdk 0，json 1 配置文件里配置决定
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 4. 1字节 指令类型 业务内容
        out.writeByte(message.getMessageType());
        // 5. 4个字节 序列号 业务内容
        out.writeInt(message.getSequenceId());
        // 无意义，填充对齐
        out.writeByte(0xff);
        // 6. 获取内容的字节数组 jdk方式
        byte[] bytes = Config.getSerializerAlgorithm().serialize(message);
        // 7. 长度
        out.writeInt(bytes.length);
        //log.debug("编码方法，内容长度：{}",bytes.length);
        // 8. 写如内容
        out.writeBytes(bytes);
        return out;
    }
}
