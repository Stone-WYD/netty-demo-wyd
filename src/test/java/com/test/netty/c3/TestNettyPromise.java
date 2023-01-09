package com.test.netty.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class TestNettyPromise {

    public static void main(String[] args) throws Exception {
        //1. 准备一个EventLoop
        EventLoop eventLoop = new NioEventLoopGroup().next();
        //2. 可以主动创建 promise，结果容器
        Promise<Integer> promise = new DefaultPromise<>(eventLoop);
        new Thread(()->{
            log.debug("开始计算...");
            try {
                int i = 1 / 0;
                Thread.sleep(1000);
                promise.setSuccess(100);
            } catch (Exception e) {
                promise.setFailure(e);
                //throw new RuntimeException(e);
            }
        }).start();

        log.debug("等待结果...");
        log.debug("取到的结果为：{}",promise.get());
    }
}
