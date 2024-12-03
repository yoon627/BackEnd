package com.devonoff.studytimeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.studyTimeline.dto.StudyTimelineDto;
import com.devonoff.domain.studyTimeline.entity.StudyTimeline;
import com.devonoff.domain.studyTimeline.repository.StudyTimelineRepository;
import com.devonoff.domain.studyTimeline.service.StudyTimelineService;
import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StudyTimelineServiceTest {

  @InjectMocks
  private StudyTimelineService studyTimelineService;

  @Mock
  private StudyTimelineRepository studyTimelineRepository;

  @Mock
  private TotalStudyTimeRepository totalStudyTimeRepository;

  @Mock
  private StudyRepository studyRepository;

  @Mock
  private AuthService authService;

  @Mock
  private StudentRepository studentRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("학습했던 스터디 타임라인 조회 - 성공")
  void testFindAllStudyTimelines_Success() {
    Long studyId = 1L;
    Long userId = 1L;
    String studyName = "Math Study";

    when(authService.getLoginUserId()).thenReturn(userId);
    when(studentRepository.existsByUserIdAndStudyId(userId, studyId)).thenReturn(true);
    when(studyRepository.findById(studyId)).thenReturn(
        Optional.ofNullable(Study.builder().id(studyId).studyName(studyName).build()));

    StudyTimeline studyTime1 = StudyTimeline.builder()
        .studyId(studyId)
        .startedAt(LocalDateTime.now().minusHours(1))
        .endedAt(LocalDateTime.now())
        .build();

    StudyTimeline studyTime2 = StudyTimeline.builder()
        .studyId(studyId)
        .startedAt(LocalDateTime.now().minusHours(2))
        .endedAt(LocalDateTime.now().minusHours(1))
        .build();

    when(studyTimelineRepository.findAllByStudyIdAndEndedAtIsNotNull(studyId))
        .thenReturn(Arrays.asList(studyTime1, studyTime2));

    List<StudyTimelineDto> result = studyTimelineService.findAllStudyTimelines(studyId);

    assertEquals(2, result.size());
    assertEquals(studyName, result.get(0).getStudyName());
    verify(authService, times(1)).getLoginUserId();
    verify(studentRepository, times(1)).existsByUserIdAndStudyId(userId, studyId);
    verify(studyRepository, times(1)).findById(studyId);
    verify(studyTimelineRepository, times(1)).findAllByStudyIdAndEndedAtIsNotNull(studyId);
  }

  @Test
  @DisplayName("학습했던 스터디 타임라인 조회 - 실패(권한 없음)")
  void testFindAllStudyTimelines_UnauthorizedAccess() {
    Long studyId = 1L;
    Long userId = 1L;

    when(authService.getLoginUserId()).thenReturn(userId);
    when(studentRepository.existsByUserIdAndStudyId(userId, studyId)).thenReturn(false);

    CustomException exception = assertThrows(CustomException.class, () -> {
      studyTimelineService.findAllStudyTimelines(studyId);
    });

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
    verify(authService, times(1)).getLoginUserId();
    verify(studentRepository, times(1)).existsByUserIdAndStudyId(userId, studyId);
    verifyNoInteractions(studyRepository);
  }

  @Test
  @DisplayName("학습시간을 누적시간에 추가 - 성공")
  void testSaveStudyTimeline_Success() {
    Long studyId = 1L;
    LocalDateTime startedAt = LocalDateTime.now().minusHours(1);
    LocalDateTime endedAt = LocalDateTime.now();
    TotalStudyTime totalStudyTime = TotalStudyTime.builder().studyId(studyId).totalStudyTime(3600L)
        .build();
    when(totalStudyTimeRepository.findById(studyId)).thenReturn(Optional.of(totalStudyTime));
    when(studyTimelineRepository.save(any(StudyTimeline.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    StudyTimeline savedStudyTimeline = studyTimelineService.saveStudyTimeline(studyId, startedAt,
        endedAt);

    assertNotNull(savedStudyTimeline);
    assertEquals(studyId, savedStudyTimeline.getStudyId());
    assertEquals(startedAt, savedStudyTimeline.getStartedAt());
    assertEquals(endedAt, savedStudyTimeline.getEndedAt());
    assertEquals(7200L, totalStudyTime.getTotalStudyTime()); // Updated to 2 hours

    verify(totalStudyTimeRepository, times(1)).findById(studyId);
    verify(totalStudyTimeRepository, times(1)).save(totalStudyTime);
    verify(studyTimelineRepository, times(1)).save(any(StudyTimeline.class));
  }

  @Test
  @DisplayName("학습시간을 누적시간에 추가 - 실패 (존재하지 않는 스터디)")
  void testSaveStudyTimeline_StudyNotFound() {
    Long studyId = 1L;
    LocalDateTime startedAt = LocalDateTime.now().minusHours(1);
    LocalDateTime endedAt = LocalDateTime.now();

    when(totalStudyTimeRepository.findById(studyId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      studyTimelineService.saveStudyTimeline(studyId, startedAt, endedAt);
    });

    assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
    verify(totalStudyTimeRepository, times(1)).findById(studyId);
    verifyNoInteractions(studyTimelineRepository);
  }
}