package com.devonoff.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.studyPost.entity.StudyComment;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyCommentRepository;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studyPost.repository.StudyReplyRepository;
import com.devonoff.domain.studyPost.service.StudyPostService;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.util.TimeProvider;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
  private StudyCommentRepository studyCommentRepository;

  @Mock
  private StudyReplyRepository studyReplyRepository;

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

  @DisplayName("배치 작업 성공 - 모집 기한이 지난 모집글 취소, 댓글/대댓글 삭제 및 모집글 삭제")
  @Test
  void deleteTasklet_Success() throws Exception {
    // Given
    LocalDateTime fixedNow = LocalDateTime.of(2024, 12, 5, 22, 29, 11);
    when(timeProvider.now()).thenReturn(fixedNow);

    LocalDateTime oneWeekAgo = fixedNow.minusDays(7);

    doNothing().when(studyPostService).cancelStudyPostIfExpired();

    StudyPost studyPost = new StudyPost();
    when(studyPostRepository.findAllByStatusAndUpdatedAtBefore(
        eq(StudyPostStatus.CANCELED), eq(oneWeekAgo))
    ).thenReturn(Collections.singletonList(studyPost));

    doNothing().when(studyPostRepository).deleteAll(any());

    // When
    RepeatStatus status = batchConfig.deleteTasklet().execute(null, mock(ChunkContext.class));

    // Then
    verify(timeProvider, times(1)).now();
    verify(studyPostService, times(1)).cancelStudyPostIfExpired();
    verify(studyPostRepository, times(1))
        .findAllByStatusAndUpdatedAtBefore(eq(StudyPostStatus.CANCELED), eq(oneWeekAgo));
    verify(studyPostRepository, times(1)).deleteAll(any());
    assert status == RepeatStatus.FINISHED;
  }

  @DisplayName("배치 작업 성공 - 1주일 지난 모집글 정상 삭제")
  @Test
  void deleteTasklet_DeletesPostsOlderThanOneWeek() throws Exception {
    // Given
    LocalDateTime fixedNow = LocalDateTime.of(2024, 12, 12, 10, 0, 0);
    when(timeProvider.now()).thenReturn(fixedNow);

    LocalDateTime oneWeekAgo = fixedNow.minusDays(7);

    StudyPost studyPost = new StudyPost();
    studyPost.setStatus(StudyPostStatus.CANCELED);
    studyPost.setUpdatedAt(oneWeekAgo.minusMinutes(1)); // 1주일 이상 지난 데이터

    when(studyPostRepository.findAllByStatusAndUpdatedAtBefore(
        eq(StudyPostStatus.CANCELED), eq(oneWeekAgo))
    ).thenReturn(List.of(studyPost));

    doNothing().when(studyPostRepository).deleteAll(any());

    // When
    RepeatStatus status = batchConfig.deleteTasklet().execute(null, mock(ChunkContext.class));

    // Then
    verify(timeProvider, times(1)).now();
    verify(studyPostRepository, times(1))
        .findAllByStatusAndUpdatedAtBefore(eq(StudyPostStatus.CANCELED), eq(oneWeekAgo));
    verify(studyPostRepository, times(1)).deleteAll(any());
    assert status == RepeatStatus.FINISHED;
  }
}