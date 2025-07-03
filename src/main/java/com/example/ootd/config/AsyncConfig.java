package com.example.ootd.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableRetry
@EnableAsync
public class AsyncConfig {

  @Value("${weather.batch.thread-pool.core-size:5}")
  private int batchCorePoolSize;

  @Value("${weather.batch.thread-pool.max-size:10}")
  private int batchMaxPoolSize;

  @Bean(name = "notificationSingleExecutor") //단일 알림용   따로 비동기 쓸꺼면 만드세요
  public Executor notificationSingleExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);//일단 그냥 있는거로
    executor.setMaxPoolSize(16);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("notificationSingleExecutor-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  @Bean(name = "notificationBulkExecutor") //벌크 알림용   따로 비동기 쓸꺼면 만드세요
  public Executor notificationBulkExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);//일단 그냥 있는거로
    executor.setMaxPoolSize(16);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("notificationBulkExecutor-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  @Bean(name = "sseBulkExecutor") //벌크 푸시용   따로 비동기 쓸꺼면 만드세요
  public Executor sseBulkExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);//일단 그냥 있는거로
    executor.setMaxPoolSize(16);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("sseBulkExecutor-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  @Bean(name = "ssePushExecutor") //알람 푸시용   따로 비동기 쓸꺼면 만드세요
  public Executor ssePushExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);//일단 그냥 있는거로
    executor.setMaxPoolSize(16);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("ssePushExecutor-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  @Bean(name = "weatherBatchTaskExecutor") // 날씨 batch 용
  public TaskExecutor weatherBatchTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(batchCorePoolSize);
    executor.setMaxPoolSize(batchMaxPoolSize);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("weather-batch-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }
}
