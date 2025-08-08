package com.example.projects__syncclipboardjava;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 同步执行带超时的操作
 */
public class TimeoutOperationSync {
    private final ExecutorService executor;

    public TimeoutOperationSync() {
        // 单线程执行器，每次执行完自动复用线程
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("timeout-operation-thread");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 执行任务，超时则抛出 TimeoutException
     *
     * @param task
     *            要执行的逻辑（返回值可为 null）
     * @param timeout
     *            超时时间
     * @param unit
     *            时间单位
     * @param <T>
     *            返回类型
     * 
     * @return 任务返回值
     * 
     * @throws TimeoutException
     *             超时
     * @throws Exception
     *             任务内部异常
     */
    public <T> T runWithTimeout(Callable<T> task, long timeout, TimeUnit unit) throws Exception {
        Future<T> future = executor.submit(task);
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true); // 中断任务线程
            throw e;
        } catch (ExecutionException e) {
            // 还原原始异常
            Throwable cause = e.getCause();
            if (cause instanceof Exception)
                throw (Exception) cause;
            if (cause instanceof Error)
                throw (Error) cause;
            throw new RuntimeException(cause);
        }
    }

    /**
     * Runnable 版本，无返回值
     */
    public void runWithTimeout(Runnable task, long timeout, TimeUnit unit) throws Exception {
        runWithTimeout(Executors.callable(task, null), timeout, unit);
    }

    /**
     * 关闭执行器
     */
    public void shutdown() {
        executor.shutdownNow();
    }
}
