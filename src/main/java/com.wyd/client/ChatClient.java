package com.wyd.client;

import com.wyd.message.*;
import com.wyd.protocol.MessageCodecSharable;
import com.wyd.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {

    public static void main(String[] args) {

        NioEventLoopGroup group = new NioEventLoopGroup();
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        AtomicBoolean LOGIN = new AtomicBoolean(false);
        AtomicBoolean EXIT = new AtomicBoolean(false);

        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);

        Scanner scanner = new Scanner(System.in);


        try {
            Bootstrap bootstrap = new Bootstrap();
            Channel channel = bootstrap.channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // 避免粘包半包
                            ch.pipeline().addLast(new ProcotolFrameDecoder());

                            // 解析消息内容
                            ch.pipeline().addLast(MESSAGE_CODEC);

                            // 用来判断是不是 读或者写空闲时间过长
                            // 3s 内如果没有向服务器写数据，会触发一个IdeState#WRITER_IDIE 事件
                            //                                         读空闲时间             写空闲时间             所有(读写)空闲时间
                            ch.pipeline().addLast(new IdleStateHandler(0,3,0));
                            // ChannelDuplexHandler 可以同时作为入站和出站处理器
                            ch.pipeline().addLast(new ChannelDuplexHandler(){
                                // 用来出发特殊事件
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if (IdleState.WRITER_IDLE == event.state()){
                                        //发送一个心跳包
                                        log.debug("3s 没写数据了，发送一个心跳包...");
                                        ctx.writeAndFlush(new PingMessage());
                                    }
                                }
                            });
                            ch.pipeline().addLast("client handler",new ChannelInboundHandlerAdapter(){
                                // 连接刚建立时触发
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    //输入登录信息
                                    new Thread(()->{
                                        System.out.println("请输入用户名：");
                                        String username = scanner.nextLine();
                                        if (EXIT.get()) {
                                            return;
                                        }
                                        System.out.println("请输入密码：");
                                        String password = scanner.nextLine();
                                        if (EXIT.get()) {
                                            return;
                                        }
                                        LoginRequestMessage message = new LoginRequestMessage(username, password);
                                        System.out.println(message);
                                        ctx.writeAndFlush(message);

                                        System.out.println("等待后续操作...");

                                        try {
                                            WAIT_FOR_LOGIN.await();
                                        }catch (InterruptedException e){
                                            e.printStackTrace();
                                        }

                                        // 如果登录失败
                                        if (!LOGIN.get()){
                                            ctx.channel().close();
                                            return;
                                        }

                                        while (true){
                                            System.out.println("==================================");
                                            System.out.println("send [username] [content]");
                                            System.out.println("gsend [group name] [content]");
                                            System.out.println("gcreate [group name] [m1,m2,m3...]");
                                            System.out.println("gmembers [group name]");
                                            System.out.println("gjoin [group name]");
                                            System.out.println("gquit [group name]");
                                            System.out.println("quit");
                                            System.out.println("==================================");
                                            String command = null;
                                            try {
                                                command = scanner.nextLine();
                                            }catch (Exception e){
                                                break;
                                            }

                                            String[] s = command.split(" ");
                                            switch(s[0]){
                                                case "send":
                                                    ctx.writeAndFlush(new ChatRequestMessage(username,s[1],s[2]));
                                                    break;
                                                case "gsend":
                                                    ctx.writeAndFlush(new GroupChatRequestMessage(username,s[1],s[2]));
                                                    break;
                                                case "gcreate":
                                                    Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                                    set.add(username);
                                                    ctx.writeAndFlush(new GroupCreateRequestMessage( s[1], set ));
                                                    break;
                                                case "gmembers":
                                                    ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                                    break;
                                                case "gjoin":
                                                    ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                                    break;
                                                case "gquit":
                                                    ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                                    break;
                                                case "quit":
                                                    ctx.channel().close();
                                                    break;
                                            }
                                        }
                                    } ,"system in").start();
                                }

                                // 读事件触发
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    // 如果消息是登录消息
                                    if (msg instanceof LoginResponseMessage) {
                                        LoginResponseMessage loginResponse = (LoginResponseMessage) msg;
                                        if (loginResponse.isSuccess()){
                                            LOGIN.set(true);
                                        }
                                        //唤醒 system in 线程
                                        WAIT_FOR_LOGIN.countDown();
                                    }
                                    // 如果消息是其他类型
                                    ClientResponseMessageHandler.clientResponseMessageHandler(ctx, msg);
                                }

                                // 在连接断开时触发
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.debug("连接已经断开，按任意键推出...");
                                    EXIT.set(true);
                                }

                                // 在出现异常时触发

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    log.debug("连接已经断开，按任意键推出...{}", cause.getCause());
                                    EXIT.set(true);
                                }
                            });
                        }
                    }).connect("localhost",8080).sync().channel();
            channel.closeFuture().sync();
        }catch (Exception e){
            log.error("client error", e);
        }finally {
            group.shutdownGracefully();
        }
    }




}
