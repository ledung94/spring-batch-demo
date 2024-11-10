package com.example.report.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.stereotype.Component;

@Component
public class JobParametersListener implements JobExecutionListener {

    private JobParameters jobParameters;
    private long startTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = System.currentTimeMillis();
        System.out.println("Job started at: " + startTime);
        this.jobParameters = jobExecution.getJobParameters();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Do nothing
        JobParameters jobParameters = jobExecution.getJobParameters();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        long readCount = jobExecution.getStepExecutions().stream()
                .mapToLong(stepExecution -> stepExecution.getReadCount())
                .sum();
        long writeCount = jobExecution.getStepExecutions().stream()
                .mapToLong(stepExecution -> stepExecution.getWriteCount())
                .sum();

        System.out.println("Job finished with parameters: " + jobParameters);
        System.out.println("Read count: " + readCount);
        System.out.println("Write count: " + writeCount);
        System.out.println("Job duration: " + duration + " milliseconds");
    }

    public JobParameters getJobParameters() {
        return this.jobParameters;
    }
}
