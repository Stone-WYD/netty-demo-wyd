package com.wyd.protocol;

import com.wyd.config.Config;
import com.wyd.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharableFit extends MessageToMessageCodec<ByteBuf, Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, List<Object> list) throws Exception {
        ByteBuf out = channelHandlerContext.alloc().buffer();
        // 编码
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

        list.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        // 解码
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();

        // 获取内容
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes,0,length);

        Class clazz = Message.getMessageClass(messageType);
        Serializer.Algorithm serializer = Serializer.Algorithm.values()[serializerType];
        Object result = serializer.deserialize(clazz, bytes);
        out.add(result);

    }
}
