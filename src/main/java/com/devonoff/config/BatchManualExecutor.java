package com.devonoff.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// BatchManualExecutor 는 애플리케이션 시작 시 배치 작업을 실행하기 위한 클래스입니다.
// 현재는 필요하지 않아 주석 처리했지만, 테스트나 수동 실행이 필요한 경우 사용할 수 있습니다.
//@Component
public class BatchManualExecutor {

  private final BatchScheduler batchScheduler;

  public BatchManualExecutor(BatchScheduler batchScheduler) {
    this.batchScheduler = batchScheduler;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void executeBatchOnStartup() {
    batchScheduler.runDeleteOldStudyPostsJob();
    System.out.println("Batch job executed on application startup.");
  }
}