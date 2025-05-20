package com.example.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class MultiThreadExam {
    private final Executor executor;

    public void execute(List<String> messages) {
        // C1: can do next task when previous task is not finished
        List<CompletableFuture<Void>> futures = messages.stream()
                .map(message -> CompletableFuture.runAsync(() -> process(message), executor))
                .toList();
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // Wait for all tasks to finish
        // .get() ném ra checked exception InterruptedException và ExecutionException, bắt buộc phải xử lý bằng try-catch
        //.join() ném ra unchecked exception CompletionException, không bắt buộc phải xử lý

        // C2: wait for all tasks to finish
        ExecutorService fixedExecutor = Executors.newFixedThreadPool(10);
        List<Future<?>> fixedFutures = new ArrayList<>();
        for (String message : messages) {
            Future<?> future = fixedExecutor.submit(() -> process(message));
            fixedFutures.add(future);
        }

        // C3: only one task at a time -> use a single thread executor
        CompletableFuture.runAsync(() -> {
            for (String message : messages) {
                try {
                    // Process each message
                    process(message);
                } catch (Exception e) {
                    log.error("Error processing message: {}", message, e);
                }
            }
        }, executor);

        // C4:
        messages.stream()
                .map(this::processMessageAsync)
                .map(future -> future
                        .orTimeout(5, TimeUnit.SECONDS)
                        .exceptionally(ex -> {
                            log.warn("Failed to process message: {}", ex.getMessage());
                            return "Failed processing";
                        }))
                .toList();

        // Question 1: How to calculate the time taken for each task?
        // Answer: You can use System.currentTimeMillis() before and after the task execution to calculate the time taken.

        // Question 2: How to handle exceptions in CompletableFuture?
        // Answer: You can use exceptionally() or handle() methods to handle exceptions in CompletableFuture.
        // Answer: You can also use whenComplete() to execute a callback after the task is completed, regardless of success or failure.
        // Answer: You can also use handle() to handle exceptions and return a default value if an exception occurs.

        // Question 3: runAsync vs supplyAsync?
        // Answer: runAsync() is used for tasks that do not return a result, while supplyAsync() is used for tasks that return a result.

        // Question 4: How to cancel a CompletableFuture?
        // Answer: You can use cancel() method to cancel a CompletableFuture. However, it will only cancel the task if it has not started yet.

        // Question 5: @Async purpose
        // Answer: avoid blocking the main thread and allow the method to run asynchronously in a separate thread.
        //


        // Timeout handle
        List<CompletableFuture<Void>> futures1 = messages.stream()
                .map(message -> CompletableFuture.runAsync(() -> process(message), executor)
                        .orTimeout(1000, TimeUnit.MILLISECONDS)
                        .exceptionally(ex -> {
                            // Handle the exception ....
                            log.error("Error processing message: {}", message, ex);
                            return null;
                        }))
                .toList();

    }

    private void process(String message) {
        // do something with the message
        System.out.println("Processing message: " + message);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> processMessageAsync(String message) {
        log.info("Processing message asynchronously: {}", message);
        try {
            // Simulate processing time
            TimeUnit.SECONDS.sleep(2);

            // Simulate occasional failures
            if (message.contains("error")) {
                throw new RuntimeException("Error processing: " + message);
            }

            return CompletableFuture.completedFuture("Processed: " + message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Error while processing message: {}", message, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public void demonstrateExceptionally() { // process only failure case: return new value thay the va su dung trong CompletableFuture
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    if (Math.random() > 0.5) {
                        throw new RuntimeException("Processing failed");
                    }
                    return "Success";
                }, executor)
                .exceptionally(ex -> {
                    log.error("Error occurred: {}", ex.getMessage());
                    return "Default value after error";
                });

        // Get the result when needed
        String result = future.join();
        log.info("Result: {}", result); // Either "Success" or "Default value after error"
    }

    public void demonstrateHandle() { // process both success and failure case: return new value thay the cho original result
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    if (Math.random() > 0.5) {
                        throw new RuntimeException("Processing failed");
                    }
                    return "Success";
                }, executor)
                .handle((result, ex) -> {
                    if (ex != null) {
                        log.error("Error occurred: {}", ex.getMessage());
                        return "Default value after error";
                    }
                    return result + " with additional processing";
                });

        String result = future.join();
        log.info("Result: {}", result);
    }

    public void demonstrateWhenComplete() { // only log ma k xu ly ket qua/exception
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    if (Math.random() > 0.5) {
                        throw new RuntimeException("Processing failed");
                    }
                    return "Success";
                }, executor)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Task failed with error: {}", ex.getMessage());
                    } else {
                        log.info("Task completed successfully with result: {}", result);
                    }
                });

        try {
            String result = future.join(); // May throw CompletionException
            log.info("Final result: {}", result);
        } catch (Exception e) {
            log.error("Exception when joining: {}", e.getMessage());
        }
    }

    public void executeWithTimeout(List<String> messages, long timeoutMs) { // It khi su dung
        // Create futures with timeout handling
        List<CompletableFuture<Void>> futures = messages.stream()
                .map(message -> {
                    // Create the main processing task
                    CompletableFuture<Void> processingFuture =
                            CompletableFuture.runAsync(() -> process(message), executor);

                    // Create a timeout task
                    CompletableFuture<Void> timeoutFuture = CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(timeoutMs);
                            if (!processingFuture.isDone()) {
                                boolean cancelled = processingFuture.cancel(true);
                                if (cancelled) {
                                    log.warn("Task for message '{}' was cancelled after timeout of {}ms",
                                            message, timeoutMs);
                                }
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, executor);

                    return processingFuture;
                })
                .toList();

        // Wait for all tasks to complete (including those that might get cancelled)
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join();
            log.info("All tasks completed successfully");
        } catch (Exception e) {
            log.error("Some tasks failed or were cancelled: {}", e.getMessage());

            // Count how many tasks were cancelled
            long cancelledCount = futures.stream()
                    .filter(CompletableFuture::isCancelled)
                    .count();
            log.info("{} out of {} tasks were cancelled due to timeout",
                    cancelledCount, futures.size());
        }
    }
}
