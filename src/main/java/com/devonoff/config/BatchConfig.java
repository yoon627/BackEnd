package com.devonoff.config;

import com.devonoff.studyPost.repository.StudyPostRepository;
import com.devonoff.type.StudyStatus;
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
      LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

      // updatedAt 기준으로 삭제
      studyPostRepository.deleteByStatusAndUpdatedAtBefore(StudyStatus.DELETION_SCHEDULED,
          oneWeekAgo);

      System.out.println("스터디 모집글 삭제 완료");
      return RepeatStatus.FINISHED;
    };
  }
}