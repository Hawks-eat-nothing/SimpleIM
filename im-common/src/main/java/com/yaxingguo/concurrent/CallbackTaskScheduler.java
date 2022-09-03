package com.yaxingguo.concurrent;

import com.google.common.util.concurrent.*;
import com.yaxingguo.util.ThreadUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class CallbackTaskScheduler {

    //使用自建线程池时，专用于处理耗时操作
    static ListeningExecutorService guavaPool = null;

    static {
        ExecutorService jPool = ThreadUtil.getMixedTargetThreadPool();
        guavaPool = MoreExecutors.listeningDecorator(jPool);
    }

    private CallbackTaskScheduler(){
    }

    /**
     * 添加任务
     * @param executeTask
     */
    public static <R> void add(CallbackTask<R> executeTask) {
        ListenableFuture<R> future = guavaPool.submit(new Callable<R>() {
            public R call() throws Exception {

                R r = executeTask.execute();
                return r;
            }

        });

        Futures.addCallback(future, new FutureCallback<R>() {
            public void onSuccess(R r) {
                executeTask.onBack(r);
            }

            public void onFailure(Throwable t) {
                executeTask.onException(t);
            }
        });
    }


}
