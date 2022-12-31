package com.test.nio.c5;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.test.nio.ByteBufferUtil.debugAll;

@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT, null);
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0 ; i<workers.length;i++){
            workers[i] = new Worker("worker-" + i);
        }
        AtomicInteger index = new AtomicInteger();
        while (true){
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey sk = iter.next();
                iter.remove();
                if (sk.isAcceptable()){
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}",sc.getRemoteAddress());
                    // 关联selector
                    log.debug("before register...{}",sc.getRemoteAddress());
                    //round robin 轮询
                    workers[index.getAndIncrement()% workers.length].register(sc);
                    log.debug("after register...{}",sc.getRemoteAddress());
                }
            }


        }



    }

    static class Worker implements Runnable{

        private Thread thread;
        private Selector selector;
        private String name;

        private volatile boolean start = false;
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        public Worker(String name){
            this.name = name;
        }

        public void register(SocketChannel sc) throws IOException {
            if (!start){
                selector = Selector.open();
                thread = new Thread(this,name);
                thread.start();
                start = true;
            }
            queue.add(()->{
                try {
                    sc.register(selector, SelectionKey.OP_READ,null);
                } catch (ClosedChannelException e) {
                    throw new RuntimeException(e);
                }
            });
            selector.wakeup();
        }

        @Override
        public void run() {
            while (true){
                try {
                    selector.select();
                    Runnable task = queue.poll();
                    if (task!=null) task.run();
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey sk = iter.next();
                        iter.remove();
                        if (sk.isReadable()){
                            SocketChannel sc = (SocketChannel) sk.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            log.debug("read...{}",sc.getRemoteAddress());
                            sc.read(buffer);
                            buffer.flip();
                            debugAll(buffer);

                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
}
