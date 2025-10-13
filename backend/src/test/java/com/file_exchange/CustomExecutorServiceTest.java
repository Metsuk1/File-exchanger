package com.file_exchange;

import com.file_exchange.executor.CustomExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CustomExecutorServiceTest {
    private CustomExecutorService platformExecutor;
    private CustomExecutorService virtualExecutor;
    private CustomExecutorService virtualPerTaskExecutor;
    private final int poolSize = 4;
    private final int timeoutSeconds = 5;

    @BeforeEach
    void setUp() {
        // Initialize executors with platform and virtual threads
        platformExecutor = CustomExecutorService.newPlatformThreadPool(poolSize);
        virtualExecutor = CustomExecutorService.newVirtualThreadPool(poolSize);
        virtualPerTaskExecutor = CustomExecutorService.newVirtualThreadPerTaskExecutor();
    }

    @Test
    @Timeout(timeoutSeconds)
    void testExecuteWithPlatformThreads() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        int taskCount = 100;
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < taskCount; i++) {
            Future<?> future = platformExecutor.submit(() -> counter.incrementAndGet());
            futures.add(future);
        }

        // waiting for all tasks to be completed.
        for (Future<?> future : futures) {
            try {
                future.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                // ignore errors
            }
        }

        platformExecutor.shutdown();
        assertTrue(platformExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS));
        assertEquals(taskCount, counter.get(), "All tasks should be exduted");
    }

    @Test
    @Timeout(timeoutSeconds)
    void testExecuteWithVirtualThreads() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        int taskCount = 100;
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < taskCount; i++) {
            Future<?> future = virtualExecutor.submit(() -> counter.incrementAndGet());
            futures.add(future);
        }

        // waiting for all tasks to be completed.
        for (Future<?> future : futures) {
            try {
                future.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                // ignore errors
            }
        }

        virtualExecutor.shutdown();
        assertTrue(virtualExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS));
        assertEquals(taskCount, counter.get(), "All tasks should be exd be executed");
    }

    @Test
    @Timeout(timeoutSeconds)
    void testSubmitRunnable() throws InterruptedException, ExecutionException {
        AtomicInteger counter = new AtomicInteger(0);
        Future<?> future = platformExecutor.submit(counter::incrementAndGet);

        future.get(); // Wait for task completion
        platformExecutor.shutdown();
        assertTrue(platformExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS));
        assertEquals(1, counter.get(), "Submitted task should increment counter");
    }

    @Test
    @Timeout(timeoutSeconds)
    void testSubmitCallable() throws InterruptedException, ExecutionException {
        Callable<Integer> task = () -> 42;
        Future<Integer> future = platformExecutor.submit(task);

        Integer result = future.get();
        platformExecutor.shutdown();
        assertTrue(platformExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS));
        assertEquals(42, result, "Callable should return correct result");
    }

    @Test
    @Timeout(timeoutSeconds)
    void testInvokeAll() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        int taskCount = 10;
        for (int i = 0; i < taskCount; i++) {
            final int value = i;
            tasks.add(() -> value);
        }

        List<Future<Integer>> futures = platformExecutor.invokeAll(tasks);
        platformExecutor.shutdown();
        assertTrue(platformExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS));

        assertEquals(taskCount, futures.size(), "All tasks should be submitted");
        for (int i = 0; i < taskCount; i++) {
            assertEquals(i, futures.get(i).get(), "Task should return correct value");
        }
    }

    @Test
    @Timeout(timeoutSeconds)
    void testInvokeAny() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        tasks.add(() -> { Thread.sleep(100); return 1; });
        tasks.add(() -> { Thread.sleep(50); return 2; });
        tasks.add(() -> { Thread.sleep(150); return 3; });

        Integer result = platformExecutor.invokeAny(tasks);
        platformExecutor.shutdown();
        assertTrue(platformExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS));
        assertTrue(List.of(1, 2, 3).contains(result), "Result should be from one of the tasks");
    }

    @Test
    void testShutdownBehavior() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        Future<?> future = platformExecutor.submit(() -> {
            try {
                Thread.sleep(1000);
                counter.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // waiting for all tasks to be completed.
        try {
            future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            // ignore errors
        }

        platformExecutor.shutdown();
        assertTrue(platformExecutor.isShutdown(), "Executor should be shut down");
        assertTrue(platformExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS));
        assertTrue(platformExecutor.isTerminated(), "Executor should be terminated");
        assertEquals(1, counter.get(), "Task should have executed");
    }

    @Test
    void testShutdownNow() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);


        platformExecutor.execute(() -> {
            try {
                Thread.sleep(5000); // Long-running task
                counter.incrementAndGet();
            } catch (InterruptedException ignored) {
            }
        });

        platformExecutor.shutdownNow();

        assertTrue(platformExecutor.isShutdown(), "Executor should be shut down");

        Thread.sleep(100);

        assertEquals(0, counter.get(), "Task should be interrupted");
    }

    @Test
    void testRejectedExecutionAfterShutdown() {
        platformExecutor.shutdown();
        assertThrows(RejectedExecutionException.class, () ->
                        platformExecutor.execute(() -> {}),
                "Should throw RejectedExecutionException after shutdown");
    }

    @Test
    void testStartCalledTwice() {
        assertThrows(IllegalStateException.class, () ->
                        platformExecutor.start(),
                "Calling start() twice should throw IllegalStateException");
    }

    @Test
    void testNullTaskSubmission() {
        assertThrows(NullPointerException.class, () ->
                        platformExecutor.execute(null),
                "Executing null task should throw NullPointerException");
        assertThrows(NullPointerException.class, () ->
                        platformExecutor.submit((Runnable) null),
                "Submitting null runnable should throw NullPointerException");
        assertThrows(NullPointerException.class, () ->
                        platformExecutor.submit((Callable<?>) null),
                "Submitting null callable should throw NullPointerException");
    }
}
