package com.file_exchange.executor;

import java.util.concurrent.BlockingQueue;

public class Builder {
    private int corePoolSize = 1;
    private boolean useVirtualThreads = false;
    private String threadNamePrefix = "custom-worker";
    private BlockingQueue<Runnable> workQueue;

    public Builder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public Builder useVirtualThreads(boolean useVirtualThreads) {
        this.useVirtualThreads = useVirtualThreads;
        return this;
    }

    public Builder threadNamePrefix(String prefix) {
        this.threadNamePrefix = prefix;
        return this;
    }

    public Builder workQueue(BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        return this;
    }

    /**
     * create a object without start
     */
    public CustomExecutorService build() {
        return new CustomExecutorService(corePoolSize, useVirtualThreads,
                threadNamePrefix, workQueue);
    }

    /**
     * create and start (convenience method)
     */
    public CustomExecutorService buildAndStart() {
        return build().start();
    }
}

