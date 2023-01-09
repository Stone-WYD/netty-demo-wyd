package com.test.netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static com.test.netty.c4.TestTypeBuf.log;

public class TestSlice {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});

        ByteBuf s1 = buffer.slice(0, 5);
        s1.retain();
        log(s1);

        ByteBuf s2 = buffer.slice(5,5);
        s2.retain();
        log(s2);

        s1.setByte(0,'1');
        log(s1);
        log(buffer);

        System.out.println("===============================");
        buffer.release();

        log(s1);
        s1.release();
        s2.release();

    }
}
