package com.devonoff.totalstudytime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.totalstudytime.dto.TotalStudyTimeDto;
import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
import com.devonoff.domain.totalstudytime.service.TotalStudyTimeService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TotalStudyTimeServiceTest {

  @InjectMocks
  private TotalStudyTimeService totalStudyTimeService;

  @Mock
  private TotalStudyTimeRepository totalStudyTimeRepository;

  @Mock
  private StudyRepository studyRepository;

  public TotalStudyTimeServiceTest() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("특정 스터디 누적학습시간 조회 - 성공")
  void testGetTotalStudyTime_Success() {
    // Given
    Long studyId = 1L;
    String studyName = "Java Study Group";
    Study mockStudy = Study.builder().id(studyId).studyName(studyName).build();
    TotalStudyTime mockTotalStudyTime = TotalStudyTime.builder()
        .studyId(studyId)
        .totalStudyTime(7200L)
        .build();

    when(studyRepository.findById(studyId)).thenReturn(Optional.of(mockStudy));
    when(totalStudyTimeRepository.findById(studyId)).thenReturn(Optional.of(mockTotalStudyTime));

    // When
    TotalStudyTimeDto result = totalStudyTimeService.getTotalStudyTime(studyId);

    // Then
    assertNotNull(result);
    assertEquals(studyId, result.getStudyId());
    assertEquals(studyName, result.getStudyName());
    assertEquals(7200L, result.getTotalStudyTime());
    verify(studyRepository, times(1)).findById(studyId);
    verify(totalStudyTimeRepository, times(1)).findById(studyId);
  }

  @Test
  @DisplayName("특정 스터디 누적학습시간 조회 - 실패 (스터디가 존재하지 않음)")
  void testGetTotalStudyTime_StudyNotFound() {
    // Given
    Long studyId = 1L;

    when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> totalStudyTimeService.getTotalStudyTime(studyId));
    assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
    verify(studyRepository, times(1)).findById(studyId);
    verify(totalStudyTimeRepository, never()).findById(any());
  }

  @Test
  @DisplayName("전체 스터디 누적학습시간 랭킹 조회 - 성공")
  void testGetTotalStudyTimeRanking_Success() {
    // Given
    List<TotalStudyTime> mockTotalStudyTimes = Arrays.asList(
        TotalStudyTime.builder().id(1L).totalStudyTime(7200L).build(),
        TotalStudyTime.builder().id(2L).totalStudyTime(5400L).build()
    );

    Study mockStudy1 = new Study();
    mockStudy1.setId(1L);
    mockStudy1.setStudyName("Java Study Group");

    Study mockStudy2 = new Study();
    mockStudy2.setId(2L);
    mockStudy2.setStudyName("Python Study Group");

    when(totalStudyTimeRepository.findTop10ByOrderByTotalStudyTimeDesc())
        .thenReturn(mockTotalStudyTimes);
    when(studyRepository.findById(1L)).thenReturn(Optional.of(mockStudy1));
    when(studyRepository.findById(2L)).thenReturn(Optional.of(mockStudy2));

    // When
    List<TotalStudyTimeDto> result = totalStudyTimeService.getTotalStudyTimeRanking();

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Java Study Group", result.get(0).getStudyName());
    assertEquals(7200L, result.get(0).getTotalStudyTime());
    assertEquals("Python Study Group", result.get(1).getStudyName());
    assertEquals(5400L, result.get(1).getTotalStudyTime());

    verify(totalStudyTimeRepository, times(1)).findTop10ByOrderByTotalStudyTimeDesc();
    verify(studyRepository, times(1)).findById(1L);
    verify(studyRepository, times(1)).findById(2L);
  }
}
