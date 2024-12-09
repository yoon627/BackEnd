package com.devonoff.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studyPost.service.StudyPostService;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.util.TimeProvider;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

class BatchConfigTest {

  @Mock
  private StudyPostRepository studyPostRepository;

  @Mock
  private StudyPostService studyPostService;

  @Mock
  private TimeProvider timeProvider;

  @InjectMocks
  private BatchConfig batchConfig;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @DisplayName("배치 작업 성공 - 모집 기한이 지난 모집글 취소 및 삭제")
  @Test
  void deleteTasklet_Success() throws Exception {
    // Given
    LocalDateTime fixedNow = LocalDateTime.of(2024, 12, 5, 22, 29, 11);
    when(timeProvider.now()).thenReturn(fixedNow);

    LocalDateTime oneWeekAgo = fixedNow.minusDays(7);

    doNothing().when(studyPostService).cancelStudyPostIfExpired();
    doNothing().when(studyPostRepository).deleteByStatusAndUpdatedAtBefore(
        eq(StudyPostStatus.CANCELED), eq(oneWeekAgo)
    );

    // When
    RepeatStatus status = batchConfig.deleteTasklet().execute(null, mock(ChunkContext.class));

    // Then
    verify(timeProvider, times(1)).now();
    verify(studyPostService, times(1)).cancelStudyPostIfExpired();
    verify(studyPostRepository, times(1))
        .deleteByStatusAndUpdatedAtBefore(eq(StudyPostStatus.CANCELED), eq(oneWeekAgo));
    assert status == RepeatStatus.FINISHED;
  }
}