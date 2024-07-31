package com.test.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * @author xh
 * @date 2024-07-30
 * @Description:
 */
@Slf4j
public class SystemUtil {

    public static void showThreadsInfo() {
        // 获取ThreadMXBean的实例
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        // 获取所有活动线程的ID
        long[] threadIds = threadMXBean.getAllThreadIds();

        // 获取每个线程的详细信息
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds);

        // 打印线程信息
        for (ThreadInfo threadInfo : threadInfos) {
            if (threadInfo != null) {
                log.error("----------------------------------------");
                log.error("Thread ID: " + threadInfo.getThreadId());
                log.error("Thread Name: " + threadInfo.getThreadName());
                log.error("Thread State: " + threadInfo.getThreadState());
                log.error("----------------------------------------");
            }
        }
    }
}
