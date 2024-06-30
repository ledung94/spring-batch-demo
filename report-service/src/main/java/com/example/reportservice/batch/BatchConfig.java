package com.example.reportservice.batch;


import com.example.reportservice.batch.entity.MsgLog;
import com.example.reportservice.batch.entity.MsgLogBak;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@Import(JobParametersListener.class)
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public Job endOfMonthJob(JobParametersListener jobParametersListener) {
        System.out.println("endOfMonthJob");
        return jobBuilderFactory.get("endOfMonthJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobParametersListener)
                .start(insertDataStep())
                .next(deleteDataStep())
                .build();
    }

    @Bean
    public Step insertDataStep() {
        return stepBuilderFactory.get("insertDataStep")
                .<MsgLog, MsgLogBak>chunk(1000)
                .reader(yourDataReader(null))
                .processor(yourDataProcessor())
                .writer(yourDataWriter())
                .build();
    }

    @Bean
    public Step deleteDataStep() {
        return stepBuilderFactory.get("deleteDataStep")
                .tasklet(deleteDataTasklet())
                .build();
    }

    @Bean
    public Tasklet deleteDataTasklet() {
        return (contribution, chunkContext) -> {
            StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
            JobParameters jobParameters = stepExecution.getJobParameters();

            // Lấy giá trị của tham số 'currentDate' từ JobParameters
            Date currentDate = jobParameters.getDate("currentDate");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = dateFormat.format(currentDate);

            String query = "DELETE FROM MSG_LOG WHERE LOG_TIME <= STR_TO_DATE('" + formattedDate + " 23:59:59', '%d-%m-%Y %H:%i:%s')";
            System.out.println(query);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.update(query);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<MsgLog> yourDataReader(@Value("#{jobParameters['currentDate']}") Date currentDate) {
        System.out.println("READ MONTH");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = dateFormat.format(currentDate);
        String query = "SELECT * FROM MSG_LOG WHERE LOG_TIME <= STR_TO_DATE('" + formattedDate + " 23:59:59', '%d-%m-%Y %H:%i:%s')";
        System.out.println(query);
        return new JdbcCursorItemReaderBuilder<MsgLog>()
                .dataSource(dataSource)
                .name("yourDataReader")
                .sql(query)
                .rowMapper(new BeanPropertyRowMapper<>(MsgLog.class))
                .build();
    }

    @Bean
    public ItemProcessor<MsgLog, MsgLogBak> yourDataProcessor() {
        return item -> {
            MsgLogBak msgLogBak = new MsgLogBak();
            BeanUtils.copyProperties(item, msgLogBak);
            return msgLogBak;
        };
    }

    @Bean
    public JdbcBatchItemWriter<MsgLogBak> yourDataWriter() {
        return new JdbcBatchItemWriterBuilder<MsgLogBak>()
                .dataSource(dataSource)
                .sql("INSERT INTO MSG_LOG_BAK (LOG_ID, CONTENT) VALUES (:id, :content)")
                .beanMapped()
                .build();
    }

    @Bean
    public Job anotherJob(JobParametersListener jobParametersListener) {
        return jobBuilderFactory.get("anotherJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobParametersListener)
                .start(yourStep())
                .build();
    }

    @Bean
    public Step yourStep() {
        System.out.println("another JOB");
        // Định nghĩa các step ở đây
        return stepBuilderFactory.get("otherStep")
                .<MsgLog, MsgLogBak>chunk(1000)
                .reader(otherReader())
                .processor(otherProcess())
                .writer(otherWrite())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<MsgLog> otherReader() {
        System.out.println("READ OTHER");
        String query = "SELECT * FROM MSG_LOG";
        System.out.println(query);
        return new JdbcCursorItemReaderBuilder<MsgLog>()
                .dataSource(dataSource)
                .name("otherReader")
                .sql(query)
                .rowMapper(new BeanPropertyRowMapper<>(MsgLog.class))
                .build();
    }

    @Bean
    public ItemProcessor<MsgLog, MsgLogBak> otherProcess() {
        return item -> {
            MsgLogBak msgLogBak = new MsgLogBak();
            BeanUtils.copyProperties(item, msgLogBak);
            return msgLogBak;
        };
    }

    @Bean
    public JdbcBatchItemWriter<MsgLogBak> otherWrite() {
        return new JdbcBatchItemWriterBuilder<MsgLogBak>()
                .dataSource(dataSource)
                .sql("INSERT INTO MSG_LOG_BAK (LOG_ID, CONTENT) VALUES (:id, :content)")
                .beanMapped()
                .build();
    }

    // Define yourDataReader, yourDataProcessor, yourDataWriter beans here
}
