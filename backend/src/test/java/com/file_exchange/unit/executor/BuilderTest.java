package com.file_exchange.unit.executor;

import static org.assertj.core.api.Assertions.assertThat;

import com.file_exchange.executor.Builder;
import com.file_exchange.executor.CustomExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Builder tests")
class BuilderTest {

    @Test
    @DisplayName("Should build executor with default values")
    void testBuildWithDefaults() {
        CustomExecutorService executor = new Builder().build();

        assertThat(executor).isNotNull();
    }

    @Test
    @DisplayName("Should build executor with custom core pool size")
    void testBuildWithCorePoolSize() {
        CustomExecutorService executor = new Builder().corePoolSize(4).build();

        assertThat(executor).isNotNull();
    }

    @Test
    @DisplayName("Should build executor with virtual threads")
    void testBuildWithVirtualThreads() {
        CustomExecutorService executor = new Builder().useVirtualThreads(true).build();

        assertThat(executor).isNotNull();
    }

    @Test
    @DisplayName("Should build executor with custom thread name prefix")
    void testBuildWithThreadNamePrefix() {
        CustomExecutorService executor =
                new Builder().threadNamePrefix("test-worker").build();

        assertThat(executor).isNotNull();
    }

    @Test
    @DisplayName("Should build executor with custom work queue")
    void testBuildWithWorkQueue() {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(100);
        CustomExecutorService executor = new Builder().workQueue(queue).build();

        assertThat(executor).isNotNull();
    }

    @Test
    @DisplayName("Should build and start executor")
    void testBuildAndStart() {
        CustomExecutorService executor = new Builder().corePoolSize(1).buildAndStart();

        assertThat(executor).isNotNull();
        executor.shutdown();
    }

    @Test
    @DisplayName("Should chain all builder methods")
    void testChainedBuilder() {
        CustomExecutorService executor = new Builder()
                .corePoolSize(2)
                .useVirtualThreads(false)
                .threadNamePrefix("chained-worker")
                .workQueue(new LinkedBlockingQueue<>(50))
                .build();

        assertThat(executor).isNotNull();
    }
}
