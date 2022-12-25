package com.test.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {
        //FileChannel
        //1. 输入输出流 2. RandomAccessFile
        try(FileChannel channel = new FileInputStream("src/test/data.txt").getChannel()) {
            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true){
                // 从channel读取数据，向buffer中写入
                int len = channel.read(buffer);
                log.info("读取到的字节数{}",len );
                if (len == -1){
                    break;
                }
                //打印buffer中内容
                //buffer切换至读模式
                buffer.flip();
                while (buffer.hasRemaining()){
                    byte b = buffer.get();
                    log.debug("实际字节{}",(char) b);
                }
                //buffer切换至写模式
                buffer.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
