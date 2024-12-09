package com.devonoff.config;

import com.devonoff.domain.study.service.StudyService;
import java.time.LocalDateTime;
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

  private final StudyService studyService;
  private final JobLauncher jobLauncher;
  private final Job deleteOldStudyPostsJob;

  @Scheduled(cron = "0 0 1 * * *") // 매일 01시에 실행
  public void runDeleteOldStudyPostsJob() {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis()) // 고유한 파라미터 추가
          .toJobParameters();
      jobLauncher.run(deleteOldStudyPostsJob, jobParameters);
      log.info("배치 작업 성공: 오래된 모집글 삭제 작업이 완료되었습니다. (실행 시간: {})", LocalDateTime.now());
    } catch (JobExecutionAlreadyRunningException | JobRestartException
             | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
      log.error("배치 작업 실패: 오래된 모집글 삭제 작업 중 오류 발생 (실행 시간: {}, 에러: {})", LocalDateTime.now(), e.getMessage(), e);
    }
  }

  @Scheduled(cron = "0 0 0 * * *") // 매일 00시에 실행
  public void updateStudyStatuses() {
    try {
      studyService.updateStudyStatuses();
      log.info("스터디 상태 업데이트 성공: 모든 스터디 상태가 갱신되었습니다. (실행 시간: {})", LocalDateTime.now());
    } catch (Exception e) {
      log.error("스터디 상태 업데이트 실패: 상태 갱신 중 오류 발생 (실행 시간: {}, 에러: {})", LocalDateTime.now(), e.getMessage(), e);
    }
  }
}