package com.example.service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
@RequiredArgsConstructor
public class AsyncConfiguration implements AsyncConfigurer {
    private static final int CORE_POOL_SIZE = 100; // default core thread pool size
    private static final int MAX_POOL_SIZE = 5000; // when queue is full, the thread pool will create new threads up to this size. Additional threads will be kept alive for 60s when idle.
    private static final int QUEUE_CAPACITY = 5000; // default queue capacity
    private final TaskExecutionProperties taskExecutionProperties;

    // 5000 tasks -> create 100 threads, 4900 tasks in queue
    // next 100 tasks -> queue size = 4900 + 100 = 5000 -> queue is full
    // next 100 tasks -> create 100 new threads, if first 100 task is not finished -> 5000 tasks in queue. If first 100 tasks are finished -> 4900 tasks in queue

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
