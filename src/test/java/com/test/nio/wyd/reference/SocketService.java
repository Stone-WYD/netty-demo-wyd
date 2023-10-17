package com.test.nio.wyd.reference;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: netty-demo-wyd
 * @description: 用于参考，助于理解nio
 * @author: Stone
 * @create: 2023-10-16 11:13
 **/
public class SocketService {

    private static final int DEFAULT_PORT = 3333;

    private final int port;

    public SocketService() {
        this(DEFAULT_PORT);
    }

    public SocketService(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        // 1.创建 ServerSocket 对象
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            // 2.调用 accept 阻塞方法，知道获取新的连接请求
            Socket socket = serverSocket.accept();
            // 3.每个新的客户端连接都需要创建一个线程，负责与客户端通信及数据的读写
            new Thread(() -> {
                try {
                    byte[] data = new byte[1024];
                    // 4.获取输入流 InputStream 对象
                    InputStream inputStream = socket.getInputStream();
                    while (true) {
                        int len = 0;
                        while ((len = inputStream.read(data)) != -1) {
                            System.out.println(new String(data, 0, len));
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    public static void main(String[] args) throws Exception {
        SocketService service = new SocketService();
        service.start();
    }
}
