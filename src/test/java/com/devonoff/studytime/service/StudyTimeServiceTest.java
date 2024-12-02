package com.devonoff.studytime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.studyTimeline.dto.StudyTimelineDto;
import com.devonoff.domain.studyTimeline.entity.StudyTimeline;
import com.devonoff.domain.studyTimeline.repository.StudyTimelineRepository;
import com.devonoff.domain.studyTimeline.service.StudyTimelineService;
import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
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

class StudyTimeServiceTest {

  @InjectMocks
  private StudyTimelineService studyTimelineService;

  @Mock
  private StudyTimelineRepository studyTimelineRepository;

  @Mock
  private TotalStudyTimeRepository totalStudyTimeRepository;

  @Mock
  private StudyRepository studyRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("학습했던 전체 시간대 조회 성공")
  void testFindAllStudyTimes_Success() {
    Long studyId = 1L;
    String studyName = "Math Study";
    Study study = new Study();
    study.setStudyName(studyName);

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

    when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
    when(studyTimelineRepository.findAllByStudyIdAndEndedAtIsNotNull(studyId))
        .thenReturn(Arrays.asList(studyTime1, studyTime2));

    List<StudyTimelineDto> result = studyTimelineService.findAllStudyTimes(studyId);

    assertEquals(2, result.size());
    assertEquals(studyName, result.get(0).getStudyName());
    verify(studyRepository, times(1)).findById(studyId);
    verify(studyTimelineRepository, times(1)).findAllByStudyIdAndEndedAtIsNotNull(studyId);
  }

  @Test
  @DisplayName("존재하지 않는 스터디로 인해 학습했던 전체 시간대 조회 실패")
  void testFindAllStudyTimes_StudyNotFound() {
    Long studyId = 1L;
    when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      studyTimelineService.findAllStudyTimes(studyId);
    });

    assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
    verify(studyRepository, times(1)).findById(studyId);
    verifyNoInteractions(studyTimelineRepository);
  }

  @Test
  @DisplayName("학습시간을 누적시간에 추가하기 성공")
  void testSaveStudyTime_Success() {
    Long studyId = 1L;
    LocalDateTime startedAt = LocalDateTime.now().minusHours(1);
    LocalDateTime endedAt = LocalDateTime.now();
    TotalStudyTime totalStudyTime = new TotalStudyTime();
    totalStudyTime.setStudyId(studyId);
    totalStudyTime.setTotalStudyTime(3600L); // Existing 1 hour

    when(totalStudyTimeRepository.findById(studyId)).thenReturn(Optional.of(totalStudyTime));
    when(studyTimelineRepository.save(any(StudyTimeline.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    StudyTimeline savedStudyTimeline = studyTimelineService.saveStudyTime(studyId, startedAt,
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
  @DisplayName("존재하지 않는 스터디로 인해 학습시간을 누적시간에 추가하기 실패")
  void testSaveStudyTime_StudyNotFound() {
    Long studyId = 1L;
    LocalDateTime startedAt = LocalDateTime.now().minusHours(1);
    LocalDateTime endedAt = LocalDateTime.now();

    when(totalStudyTimeRepository.findById(studyId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      studyTimelineService.saveStudyTime(studyId, startedAt, endedAt);
    });

    assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
    verify(totalStudyTimeRepository, times(1)).findById(studyId);
    verifyNoInteractions(studyTimelineRepository);
  }
}
