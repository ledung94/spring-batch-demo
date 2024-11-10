package com.example.report.batch;

import com.example.report.repository.MsgLogRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MonthlyJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job endOfMonthJob;
    private final Job anotherJob;
    private final MsgLogRepository logRepository;

    @Autowired
    public MonthlyJobScheduler(JobLauncher jobLauncher, Job endOfMonthJob, Job anotherJob, MsgLogRepository logRepository) {
        this.jobLauncher = jobLauncher;
        this.endOfMonthJob = endOfMonthJob;
        this.anotherJob = anotherJob;
        this.logRepository = logRepository;
    }

//    @Scheduled(cron = "0 0 0 L * ?") // Chạy vào lúc 00:00 vào ngày cuối cùng của mỗi tháng
//    @Scheduled(cron = "*/5 * * * * *") // Chạy vào lúc 00:00 vào ngày cuối cùng của mỗi tháng
    public void runEndOfMonthJob() throws Exception {
        System.out.println("RUNNNNN");
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("currentDate", new Date())
//                .addString("jobName", "anotherJob")
                .toJobParameters();
        jobLauncher.run(endOfMonthJob, jobParameters);
    }

    @Scheduled(cron = "0 04 22 * * *")
    public void runProcedure() {
        logRepository.cutOffData(0);
    }
}
