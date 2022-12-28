package com.test.netty.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost",8080));
        sc.write(Charset.defaultCharset().encode("abcdefghij\nk12345688\n88\n"));
        SocketAddress address = sc.getLocalAddress();
        System.in.read();
    }
}
