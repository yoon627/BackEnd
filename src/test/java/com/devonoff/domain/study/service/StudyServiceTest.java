package com.devonoff.domain.study.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

  @Mock
  private StudyRepository studyRepository;

  @Mock
  private StudyPostRepository studyPostRepository;

  @Mock
  private TotalStudyTimeRepository totalStudyTimeRepository;

  @Mock
  private TimeProvider timeProvider; // Mock TimeProvider

  @InjectMocks
  private StudyService studyService;

  @DisplayName("모집글 마감 시 스터디 생성 성공")
  @Test
  void createStudyFromClosedPost_Success() {
    // Given
    Long studyPostId = 1L;
    StudyPost studyPost = StudyPost.builder()
        .id(studyPostId)
        .studyName("Test Study")
        .subject(StudySubject.JOB_PREPARATION)
        .difficulty(StudyDifficulty.MEDIUM)
        .dayType(3)
        .startDate(LocalDate.of(2024, 12, 10))
        .endDate(LocalDate.of(2024, 12, 20))
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(12, 0))
        .meetingType(StudyMeetingType.ONLINE)
        .currentParticipants(5)
        .user(User.builder().id(2L).build())
        .build();

    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));
    when(studyRepository.save(any(Study.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(totalStudyTimeRepository.save(any(TotalStudyTime.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Study result = studyService.createStudyFromClosedPost(studyPostId);

    // Then
    assertNotNull(result);
    assertEquals("Test Study", result.getStudyName());
    assertEquals(StudySubject.JOB_PREPARATION, result.getSubject());
    assertEquals(StudyDifficulty.MEDIUM, result.getDifficulty());
    assertEquals(3, result.getDayType());
    assertEquals(LocalDate.of(2024, 12, 10), result.getStartDate());
    assertEquals(LocalDate.of(2024, 12, 20), result.getEndDate());
    assertEquals(LocalTime.of(10, 0), result.getStartTime());
    assertEquals(LocalTime.of(12, 0), result.getEndTime());
    assertEquals(StudyMeetingType.ONLINE, result.getMeetingType());
    assertEquals(6, result.getTotalParticipants()); // currentParticipants + 1
    assertEquals(studyPost.getUser(), result.getStudyLeader());
    assertEquals(StudyStatus.PENDING, result.getStatus());

    verify(studyPostRepository, times(1)).findById(studyPostId);
    verify(studyRepository, times(1)).save(any(Study.class));
    verify(totalStudyTimeRepository, times(1)).save(any(TotalStudyTime.class));
  }

  @DisplayName("모집글 마감 시 스터디 생성 실패 - 모집글 없음")
  @Test
  void createStudyFromClosedPost_Fail_StudyPostNotFound() {
    // Given
    Long studyPostId = 999L;
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> studyService.createStudyFromClosedPost(studyPostId));

    assertEquals(ErrorCode.STUDY_POST_NOT_FOUND, exception.getErrorCode());
    verify(studyPostRepository, times(1)).findById(studyPostId);
  }

  @Test
  void testUpdateStudyStatuses() {
    // given
    LocalDateTime fixedNow = LocalDateTime.of(2024, 12, 4, 10, 0); // Mock 현재 시간
    when(timeProvider.now()).thenReturn(fixedNow); // Mock TimeProvider로 시간 고정

    // Mock 데이터
    List<Study> pendingStudies = List.of(
        Study.builder()
            .id(1L)
            .status(StudyStatus.PENDING)
            .startDate(fixedNow.toLocalDate().minusDays(1))
            .build()
    );
    List<Study> inProgressStudies = List.of(
        Study.builder()
            .id(2L)
            .status(StudyStatus.IN_PROGRESS)
            .endDate(fixedNow.toLocalDate().minusDays(1))
            .build()
    );

    when(studyRepository.findAllByStatusAndStartDateBefore(StudyStatus.PENDING, fixedNow))
        .thenReturn(pendingStudies);
    when(studyRepository.findAllByStatusAndEndDateBefore(StudyStatus.IN_PROGRESS,
        fixedNow.toLocalDate().atStartOfDay()))
        .thenReturn(inProgressStudies);

    // when
    studyService.updateStudyStatuses();

    // then
    verify(studyRepository).findAllByStatusAndStartDateBefore(StudyStatus.PENDING, fixedNow);
    verify(studyRepository).findAllByStatusAndEndDateBefore(StudyStatus.IN_PROGRESS,
        fixedNow.toLocalDate().atStartOfDay());
    verify(studyRepository).saveAll(pendingStudies);
    verify(studyRepository).saveAll(inProgressStudies);
  }
}