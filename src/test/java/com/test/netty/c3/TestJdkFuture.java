package com.test.netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class TestJdkFuture {

    public static void main(String[] args) throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<Integer> future = executor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行任务中...");
                Thread.sleep(1000);
                return 20;
            }
        });

        log.debug("获取结果中...");
        log.debug("获取的结果为：{}",future.get());

    }
}
