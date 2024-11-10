package com.example.report.batch;

import com.example.report.batch.entity.MsgLog;
import com.example.report.batch.entity.MsgLogBak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(JobParametersListener.class)
public class BatchConfig extends DefaultBatchConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
            DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("batch-thread-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    protected JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        jobLauncher.setTaskExecutor(taskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public Job endOfMonthJob(JobParametersListener jobParametersListener) throws Exception {
        return jobBuilderFactory.get("endOfMonthJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobParametersListener)
                .start(insertDataStep())
                .next(deleteDataStep())
                .build();
    }

    @Bean
    public Step insertDataStep() throws Exception {
        return stepBuilderFactory.get("insertDataStep")
                .<MsgLog, MsgLogBak>chunk(1000)
                .reader(pagingItemReader(null))
                .processor(yourDataProcessor())
                .writer(yourDataWriter())
                .taskExecutor(taskExecutor())
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
            Date currentDate = jobParameters.getDate("currentDate");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = dateFormat.format(currentDate);
            String query = "DELETE FROM MSG_LOG WHERE LOG_TIME <= STR_TO_DATE('" + formattedDate
                    + " 23:59:59', '%d-%m-%Y %H:%i:%s')";
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.update(query);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<MsgLog> yourDataReader(@Value("#{jobParameters['currentDate']}") Date currentDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = dateFormat.format(currentDate);
        String query = "SELECT * FROM MSG_LOG WHERE LOG_TIME <= STR_TO_DATE('" + formattedDate
                + " 23:59:59', '%d-%m-%Y %H:%i:%s')";
        logger.info("SQL: {}", query);
        return new JdbcCursorItemReaderBuilder<MsgLog>()
                .dataSource(dataSource)
                .name("yourDataReader")
                .sql(query)
                .rowMapper(new BeanPropertyRowMapper<>(MsgLog.class) {
                    @Override
                    public MsgLog mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        try {
                            return super.mapRow(rs, rowNumber);
                        } catch (SQLException e) {
                            System.err.println("Error mapping row " + rowNumber + ": " + e.getMessage());
                            throw e;
                        }
                    }
                })
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<MsgLog> pagingItemReader(@Value("#{jobParameters['currentDate']}") Date currentDate)
            throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = dateFormat.format(new Date());
        parameterValues.put("toDate", formattedDate);
        return new JdbcPagingItemReaderBuilder<MsgLog>()
                .dataSource(dataSource)
                .name("pagingItemReader")
                .queryProvider(queryProvider())
                .parameterValues(parameterValues)
                .rowMapper(new BeanPropertyRowMapper<>(MsgLog.class))
                .pageSize(1000)
                .build();
    }

    @Bean
    public PagingQueryProvider queryProvider() throws Exception {
        // If using oracle database => OraclePagingQueryProvider
        // If having no sort key or data have no index => paging will be failed
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("SELECT *");
        factoryBean.setFromClause("FROM MSG_LOG");
        factoryBean.setWhereClause("WHERE LOG_TIME <= STR_TO_DATE(:toDate, '%d-%m-%Y %H:%i:%s')");
        factoryBean.setSortKey("LOG_ID");
        return factoryBean.getObject();
    }

    @Bean
    public ItemProcessor<MsgLog, MsgLogBak> yourDataProcessor() {
        return item -> {
            MsgLogBak msgLogBak = new MsgLogBak();
            // Log các thông tin để kiểm tra
            // logger.info("Đang thực hiện công việc gì đó trên thread {}",
            // Thread.currentThread().getName());
            BeanUtils.copyProperties(item, msgLogBak);
            return msgLogBak;
        };
    }

    @Bean
    public JdbcBatchItemWriter<MsgLogBak> yourDataWriter() {
        return new JdbcBatchItemWriterBuilder<MsgLogBak>()
                .dataSource(dataSource)
                .sql("INSERT INTO MSG_LOG_BAK (LOG_ID, CONTENT) VALUES (:logId, :content)")
                .assertUpdates(false)
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
        String query = "SELECT * FROM MSG_LOG";
        return new JdbcCursorItemReaderBuilder<MsgLog>()
                .dataSource(dataSource)
                .name("otherReader")
                .sql(query)
                .rowMapper(new BeanPropertyRowMapper<>(MsgLog.class) {
                    @Override
                    public MsgLog mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        try {
                            return super.mapRow(rs, rowNumber);
                        } catch (SQLException e) {
                            System.err.println("Error mapping row " + rowNumber + ": " + e.getMessage());
                            throw e;
                        }
                    }
                })
                .build();
    }

    @Bean
    public ItemProcessor<MsgLog, MsgLogBak> otherProcess() {
        return item -> {
            MsgLogBak msgLogBak = new MsgLogBak();
            // Log các thông tin để kiểm tra
            // System.out.println("Processing MsgLog: " + item);
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
}
