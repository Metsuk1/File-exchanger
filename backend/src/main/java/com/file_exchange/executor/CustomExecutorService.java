package com.file_exchange.executor;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class CustomExecutorService implements ExecutorService {
    private final int corePoolSize;
    private final boolean useVirtualThreads;
    private final String threadNamePrefix;
    private final  BlockingQueue<Runnable> workQueue;

    //these fields I will initialize in specific method
    private List<Thread> poolWorkers;
    private AtomicBoolean shutdown;
    private CountDownLatch shutdownLatch;
    private ThreadFactory threadFactory;
    private volatile boolean started = false;

    //package Constructor
   CustomExecutorService(int corePoolSize, boolean useVirtualThreads,String threadNamePrefix, BlockingQueue<Runnable> workQueue) {
        // validation
        if (corePoolSize <= 0) {
            throw new IllegalArgumentException("corePoolSize should be greater than 0");
        }

        if(threadNamePrefix == null) {
            throw new IllegalArgumentException("threadNamePrefix cannot be null");
        }

       this.corePoolSize = corePoolSize;
       this.useVirtualThreads = useVirtualThreads;
       this.threadNamePrefix = threadNamePrefix;
       this.workQueue = workQueue != null ? workQueue : new LinkedBlockingQueue<>();

    }

    /*
    It's the specific method for creation com   plex objects
     */
    public synchronized CustomExecutorService start(){
       if(started) {
           throw new IllegalStateException("CustomExecutorService already started");
       }

        // initialization complex objects
        this.poolWorkers = new ArrayList<>();
        this.shutdown = new AtomicBoolean(false);
        this.shutdownLatch = new CountDownLatch(corePoolSize);
        this.threadFactory = createThreadFactory();

        //starting workers
        initializeWorkers();

        this.started = true;

        return this;
    }

    /*
    static method for creation Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // Factory methods
    public static CustomExecutorService newPlatformThreadPool(int nThreads) {
        return builder()
                .corePoolSize(nThreads)
                .useVirtualThreads(false)
                .threadNamePrefix("platform-pool")
                .buildAndStart();
    }

    public static CustomExecutorService newVirtualThreadPool(int nThreads) {
        return builder()
                .corePoolSize(nThreads)
                .useVirtualThreads(true)
                .threadNamePrefix("virtual-pool")
                .buildAndStart();
    }

    //Creation ThreadFactory
    private ThreadFactory createThreadFactory() {
        if (useVirtualThreads) {
            return Thread.ofVirtual()
                    .name(threadNamePrefix + "-", 0)
                    .factory();
        } else {
            return Thread.ofPlatform()
                    .name(threadNamePrefix + "-", 0)
                    .daemon(false)
                    .factory();
        }
    }

    private void initializeWorkers() {
        for (int i = 0; i < corePoolSize; i++) {
            Thread worker = threadFactory.newThread(new WorkerRunnable());
            poolWorkers.add(worker);
            worker.start();
        }
    }

    //Check initialization before use
    protected void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("ExecutorService not started. Call start() first.");
        }
    }

    /**
     Worker thread  that continuously takes tasks from the queue and executes them.
     Uses blocking 'take()' — sleeps efficiently until a new task is available.
     */
    private class WorkerRunnable implements Runnable {
        @Override
        public void run() {
            // Continue while executor not shutdown or tasks remain in queue
            while(!shutdown.get() || !workQueue.isEmpty()) {
                try{
                    Runnable task = workQueue.take();// Blocks efficiently
                    task.run();
                }catch (InterruptedException e){
                    if(shutdown.get()) {
                        Thread.currentThread().interrupt();// Restore interrupt status
                        break; // Exit on shutdown interrupt
                    }
                    // Otherwise, continue (rare case)
                }catch(Throwable t){ // Catch Throwable for reliability
                    System.err.println("Task execution failed: " + t.getMessage());
                    t.printStackTrace();
                }
            }
            shutdownLatch.countDown(); // Уменьшаем счётчик в finally

        }
    }


    @Override
    public void shutdown() {
        ensureStarted();
        shutdown.set(true);
        // Interrupt all workers to wake them from take()
        for(Thread worker : poolWorkers) {
            worker.interrupt();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        ensureStarted();

        shutdown.set(true);

        for(Thread worker : poolWorkers) {
            worker.interrupt();
        }
        List<Runnable> result = new ArrayList<>();
        workQueue.drainTo(result);

        return result;
    }

    @Override
    public boolean isShutdown() {
        return started && shutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return started && shutdown.get() && shutdownLatch.getCount() == 0;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        ensureStarted();

        if (!shutdown.get()) {
            return false;
        }
        return shutdownLatch.await(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        ensureStarted();

        if(task == null) {
            throw new NullPointerException();
        }
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);

        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        ensureStarted();

        if(task == null) {
            throw new NullPointerException();
        }
        FutureTask<T> futureTask = new FutureTask<>(task, result);
        execute(futureTask);

        return futureTask;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return submit(task,null);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        ensureStarted();

        if (tasks == null) {
            throw new NullPointerException();
        }

        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }

        for (Future<T> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                // Explicitly silenced-error in one task should not stop others
            }
        }

        return futures;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return List.of();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
        ensureStarted();

        List<Future<T>> tasks = new ArrayList<>();
        for (var task : collection) {
            var future = submit(task);
            tasks.add(future);
        }
        while (true) {
            for (var future : tasks) {
                if (future.isDone()) {
                    return future.get();
                }
            }
            Thread.sleep(10);
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public void execute(Runnable command) {
        ensureStarted();

        if(command == null) {
            throw new NullPointerException("Command is null");
        }
        if(shutdown.get()) {
            throw new RejectedExecutionException("Executor shutdown");
        }

        boolean accepted = workQueue.offer(command);
        if (!accepted) {
            throw new RejectedExecutionException("Queue is full, task rejected");
        }

    }

    public static CustomExecutorService newVirtualThreadPerTaskExecutor(){
        return new  VirtualThreadPerTaskExecutor();
    }

    private static class VirtualThreadPerTaskExecutor extends CustomExecutorService {
        private final AtomicBoolean virtualShutdown = new AtomicBoolean(false);

        public VirtualThreadPerTaskExecutor() {
            // Вызываем package-private конструктор с минимальными параметрами
            super(1, true, "virtual-per-task", new LinkedBlockingQueue<>());
            super.shutdown = new AtomicBoolean(false);
        }

        @Override
        public CustomExecutorService start() {
            super.started = true;
            return this;
        }

        @Override
        public void execute(Runnable command) {
            if (command == null) {
                throw new NullPointerException("command can't be null");
            }
            if (virtualShutdown.get()) {
                throw new RejectedExecutionException("Executor shutdown");
            }

            // create new virtual thread for each task
            Thread.ofVirtual()
                    .name("virtual-task-thread")
                    .start(command);
        }

        @Override
        public void shutdown() {
            virtualShutdown.set(true);
            super.shutdown.set(true);
        }

        @Override
        public boolean isShutdown() {
            return virtualShutdown.get();
        }

        @Override
        public boolean isTerminated() {
            return virtualShutdown.get();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            Thread.sleep(unit.toMillis(timeout));
            return isTerminated();
        }

        @Override
        protected void ensureStarted() {
        }
    }


}
