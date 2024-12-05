package com.devonoff.config;

import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studyPost.service.StudyPostService;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.util.TimeProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig extends DefaultBatchConfiguration {

  private final StudyPostRepository studyPostRepository;
  private final StudyPostService studyPostService;
  private final TimeProvider timeProvider;

  @Bean
  public Job deleteOldStudyPostsJob(JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {
    return new JobBuilder("deleteOldStudyPostsJob", jobRepository)
        .start(deleteStep(jobRepository, transactionManager))
        .build();
  }

  @Bean
  public Step deleteStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {
    return new StepBuilder("deleteStep", jobRepository)
        .tasklet(deleteTasklet(), transactionManager)
        .build();
  }

  @Bean
  public Tasklet deleteTasklet() {
    return (contribution, chunkContext) -> {
      LocalDateTime oneWeekAgo = timeProvider.now().minusDays(7);

      // 모집 기한이 지난 스터디 모집글을 CANCELED 로 변경(배치작업으로 자동 취소)
      studyPostService.cancelStudyPostIfExpired();
      // 취소 상태에서 일주일이 지난 스터디 모집글 삭제
      studyPostRepository.deleteByStatusAndUpdatedAtBefore(StudyPostStatus.CANCELED, oneWeekAgo);

      return RepeatStatus.FINISHED;
    };
  }
}