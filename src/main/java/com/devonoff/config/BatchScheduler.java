package com.devonoff.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job deleteOldStudyPostsJob;

  @Scheduled(cron = "0 0 1 * * *") // 매일 01시에 실행
  public void runDeleteOldStudyPostsJob() {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis()) // 고유한 파라미터 추가
          .toJobParameters();
      jobLauncher.run(deleteOldStudyPostsJob, jobParameters);
    } catch (JobExecutionAlreadyRunningException | JobRestartException
             | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
      log.error("Batch job failed at {} due to: {}", System.currentTimeMillis(), e.getMessage(), e);
    }
  }
}