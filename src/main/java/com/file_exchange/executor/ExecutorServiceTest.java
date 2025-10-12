package com.file_exchange.executor;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorServiceTest {
    private static PrintWriter writer;

    public static void main(String[] args) throws Exception {
        try {
            writer = new PrintWriter(new FileWriter("executor-test-results.txt", false));

            // Run tests
            testPerformanceComparison();
            testConcurrentExecution();
            testShutdownBehavior();
            testVirtualThreadPerTaskExecutor();

            System.out.println("success");
        } finally {
            if (writer != null) writer.close();
        }
    }

    // ---------- TEST 1: PERFORMANCE ----------
    private static void testPerformanceComparison() throws InterruptedException {
        log("\n=== Test 1: Performance Comparison ===");

        int[] poolSizes = {10, 50, 100, 500};
        int taskCount = 10_000;
        int sleepMillis = 10;

        for (int poolSize : poolSizes) {
            log("\n--- Pool Size = " + poolSize + " ---");

            // Platform threads
            long start = System.currentTimeMillis();
            ExecutorService platformExec = CustomExecutorService.newPlatformThreadPool(poolSize);
            submitSleepTasks(platformExec, taskCount, sleepMillis);
            platformExec.shutdown();
            platformExec.awaitTermination(1, TimeUnit.HOURS);
            long platformTime = System.currentTimeMillis() - start;

            // Virtual threads
            start = System.currentTimeMillis();
            ExecutorService virtualExec = CustomExecutorService.newVirtualThreadPool(poolSize);
            submitSleepTasks(virtualExec, taskCount, sleepMillis);
            virtualExec.shutdown();
            virtualExec.awaitTermination(1, TimeUnit.HOURS);
            long virtualTime = System.currentTimeMillis() - start;

            log(String.format("Platform time: %d ms, Virtual time: %d ms", platformTime, virtualTime));

            // Memory usage
            System.gc();
            Thread.sleep(1000);
            long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            log(String.format("Memory used after poolSize=%d: %d MB", poolSize, usedMemory));
        }
    }

    private static void submitSleepTasks(ExecutorService executor, int taskCount, int sleepMillis) {
        for (int i = 0; i < taskCount; i++) {
            executor.execute(() -> {
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    // Explicitly silenced: the test intentionally ignores interruption
                    Thread.currentThread().interrupt(); // restore interrupt flag
                    log("Task interrupted (explicitly silenced): " + e.getMessage());
                }
            });
        }
    }

    // ---------- TEST 2: CONCURRENT EXECUTION ----------
    private static void testConcurrentExecution() throws InterruptedException {
        log("\n=== Test 2: Concurrent Execution ===");

        int taskCount = 1000;
        AtomicInteger counter = new AtomicInteger(0);

        // Platform threads
        ExecutorService platformExec = CustomExecutorService.newPlatformThreadPool(50);
        runIncrementTasks(platformExec, taskCount, counter);
        platformExec.shutdown();
        platformExec.awaitTermination(1, TimeUnit.MINUTES);
        log("Platform threads counter = " + counter.get());

        // Virtual threads
        counter.set(0);
        ExecutorService virtualExec = CustomExecutorService.newVirtualThreadPool(50);
        runIncrementTasks(virtualExec, taskCount, counter);
        virtualExec.shutdown();
        virtualExec.awaitTermination(1, TimeUnit.MINUTES);
        log("Virtual threads counter = " + counter.get());
    }

    private static void runIncrementTasks(ExecutorService executor, int taskCount, AtomicInteger counter) {
        for (int i = 0; i < taskCount; i++) {
            executor.execute(counter::incrementAndGet);
        }
    }

    // ---------- TEST 3: SHUTDOWN ----------
    private static void testShutdownBehavior() throws InterruptedException {
        log("\n=== Test 3: Shutdown Behavior ===");

        ExecutorService exec = CustomExecutorService.newPlatformThreadPool(5);

        // Submit tasks
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            exec.execute(() -> {
                try {
                    Thread.sleep(100);
                    log("Task " + finalI + " done");
                } catch (InterruptedException ignored) {
                }
            });
        }

        // Shutdown
        exec.shutdown();
        boolean terminated = exec.awaitTermination(2, TimeUnit.SECONDS);
        log("Is shutdown: " + exec.isShutdown());
        log("Is terminated: " + exec.isTerminated());
        log("Terminated within timeout: " + terminated);

        // Try submitting new task -> should throw exception
        try {
            exec.execute(() -> log("Should not run"));
        } catch (RejectedExecutionException e) {
            log("Correctly rejected new task after shutdown.");
        }
    }

    // ---------- TEST 4: VIRTUAL THREAD PER TASK EXECUTOR ----------
    private static void testVirtualThreadPerTaskExecutor() throws InterruptedException {
        log("\n=== Test 4: Virtual Thread Per Task Executor ===");

        ExecutorService exec = CustomExecutorService.newVirtualThreadPerTaskExecutor();
        AtomicInteger counter = new AtomicInteger(0);
        int taskCount = 1000;

        // Submit tasks
        for (int i = 0; i < taskCount; i++) {
            exec.execute(counter::incrementAndGet);
        }

        // Give some time for tasks to complete
        Thread.sleep(1000);
        log("VirtualThreadPerTaskExecutor counter = " + counter.get());

        // Shutdown
        exec.shutdown();
        boolean terminated = exec.awaitTermination(2, TimeUnit.SECONDS);
        log("Is shutdown: " + exec.isShutdown());
        log("Is terminated: " + exec.isTerminated());
        log("Terminated within timeout: " + terminated);

        // Try submitting new task -> should throw exception
        try {
            exec.execute(() -> log("Should not run"));
        } catch (RejectedExecutionException e) {
            log("Correctly rejected new task after shutdown.");
        }
    }

    private static void log(String message) {
        System.out.println(message);
        writer.println(message);
        writer.flush();
    }
}