package com.yaxingguo.concurrent;

import com.yaxingguo.util.ThreadUtil;

import java.util.concurrent.ThreadPoolExecutor;

public class FutureTaskScheduler {

    static ThreadPoolExecutor mixPool = null;

    static {
        mixPool = ThreadUtil.getMixedTargetThreadPool();
    }

    private static FutureTaskScheduler inst = new FutureTaskScheduler();

    private FutureTaskScheduler(){

    }
    /**
     * 添加任务
     */
    public static void add(ExecuteTask executeTask){
        mixPool.submit(()->{executeTask.execute();});
    }
}
