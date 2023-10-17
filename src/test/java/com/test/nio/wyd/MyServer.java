package com.test.nio.wyd;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @program: netty-demo-wyd
 * @description: nio-wyd 测试类
 * @author: Stone
 * @create: 2023-10-16 10:57
 **/
public class MyServer {

    private Selector selector;

    private static  final int DEFAULT_PORT = 3333;

    private final int port;

    public MyServer() {
        this(DEFAULT_PORT);
    }

    public MyServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置 Socket 为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 获取与该 Channel 关联的服务端 Socket
        ServerSocket serverSocket = serverSocketChannel.socket();
        // 绑定服务地址
        serverSocket.bind(new InetSocketAddress(port));

        // 获取一个 selector
        selector = Selector.open();
        // 注册 Channel 到 selector 上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 获取就绪的事件集合
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            // 处理就绪事件
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                handleEvent(selectionKey);
            }
        }

    }

    private void handleEvent(SelectionKey selectionKey) throws IOException{
        SocketChannel client;
        // 如果是连接事件
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
            client = server.accept();
            if (null == client) {
                return;
            }
            // 套接字为非阻塞模式
            client.configureBlocking(false);
            // 注册到 select 上
            client.register(selector, SelectionKey.OP_READ);

            // 如果是读事件
        } else if (selectionKey.isReadable()) {
            client = (SocketChannel) selectionKey.channel();
            ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
            receiveBuffer.clear();
            int count = client.read(receiveBuffer);
            if (count > 0) {
                System.out.println("receive client msg:" + new String(receiveBuffer.array(), 0, count));
            }

            // 发送数据到客户端
            ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
            sendBuffer.clear();
            sendBuffer.put("Hello client.".getBytes());
            sendBuffer.flip();
            client.write(sendBuffer);
            System.out.println("send msg to client:Hello client.");
        }
    }

    public static void main(String[] args) throws IOException {
        MyServer myServer = new MyServer();
        myServer.start();
    }

}
