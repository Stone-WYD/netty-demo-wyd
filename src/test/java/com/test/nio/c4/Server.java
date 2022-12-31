package com.test.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import static com.test.nio.ByteBufferUtil.debugAll;

import java.util.Iterator;

@Slf4j
public class Server {

    private static void split(ByteBuffer source) {
        source.flip();

        for (int i = 0; i < source.limit(); i++){
            if (source.get(i) == '\n') {
                //创建目标target
                int len = i + 1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(len);

                while (source.position()<=i){
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact();
    }

    public static void main(String[] args) throws IOException {
        //1. 创建 selector，管理多个 channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        //2. 建立selector与channel的联系（注册）
        SelectionKey sscKey = ssc.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey:{}",sscKey);
        ssc.bind(new InetSocketAddress(8080));

        while (true){
            //3. select 方法，没有发生事件时，线程阻塞，有事件发生时，线程才会恢复运行
            selector.select();
            //4. 处理事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                // 处理key时，要从 selectedKey中删除，否则下次处理会有问题
                iterator.remove();
                log.debug("key:{}",key);
                //5. 区分事件类型
                if (key.isAcceptable()) {//如果是accept事件
                    ServerSocketChannel channel =(ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                    SelectionKey sckey = sc.register(selector, 0, byteBuffer);
                    sckey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}",sc);
                }else if (key.isReadable()){//如果是读事件
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                        int read = sc.read(byteBuffer);
                        if (read == -1){
                            //正常断开
                            key.cancel();
                        }else {
                            split(byteBuffer);
                            if (byteBuffer.position() == byteBuffer.limit()){
                                //需要扩容
                                byteBuffer.flip();
                                ByteBuffer newBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2);
                                newBuffer.put(byteBuffer);
                                key.attach(newBuffer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //异常断开
                        key.cancel();
                    }
                }
            }
        }

    }
}
