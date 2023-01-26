package com.test.advance.c2;

import com.wyd.message.LoginRequestMessage;
import com.wyd.protocol.MessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                //                                一个包的最大字节数，   长度字段在包中位置，  长度字段长度，      长度字段距离内容的偏移量，是否剥离内容外的其他内容
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                new MessageCodec()
        );


        // encode
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        //channel.writeOutbound(message);

        // decode
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,buf);

        //入站
        /*channel.writeInbound(buf);*/

        //测试半包
        ByteBuf s1 = buf.slice(0, 100);
        ByteBuf s2 = buf.slice(100, buf.readableBytes() - 100);
        s1.retain(); //引用次数此时为2

        channel.writeInbound(s1); //引用次数此时为1
        channel.writeInbound(s2); //引用次数此时为0
    }
}
