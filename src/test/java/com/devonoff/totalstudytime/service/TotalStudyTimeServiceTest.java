package com.devonoff.totalstudytime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("특정 스터디 누적 학습시간 조회 - 성공")
  void testGetTotalStudyTime_Success() {
    // Given
    Long studyId = 1L;
    String studyName = "Java Study Group";

    TotalStudyTime mockTotalStudyTime = TotalStudyTime.builder()
        .studyId(studyId)
        .totalStudyTime(7200L)
        .build();

    Study mockStudy = Study.builder()
        .id(studyId)
        .studyName(studyName)
        .build();

    when(studyRepository.findById(studyId)).thenReturn(Optional.of(mockStudy));
    when(totalStudyTimeRepository.findAllByOrderByTotalStudyTimeDesc()).thenReturn(
        List.of(mockTotalStudyTime));
    when(totalStudyTimeRepository.findById(studyId)).thenReturn(Optional.of(mockTotalStudyTime));

    // When
    TotalStudyTimeDto result = totalStudyTimeService.getTotalStudyTime(studyId);

    // Then
    assertNotNull(result);
    assertEquals(studyId, result.getStudyId());
    assertEquals(studyName, result.getStudyName());
    assertEquals("2시간 ", result.getTotalStudyTime());
    verify(studyRepository).findById(studyId);
    verify(totalStudyTimeRepository).findById(studyId);
  }

  @Test
  @DisplayName("특정 스터디 누적 학습시간 조회 - 실패 (스터디 없음)")
  void testGetTotalStudyTime_StudyNotFound() {
    // Given
    Long studyId = 1L;

    when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> totalStudyTimeService.getTotalStudyTime(studyId));

    assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
    verify(studyRepository).findById(studyId);
    verify(totalStudyTimeRepository, never()).findById(any());
  }

  @Test
  @DisplayName("전체 스터디 누적 학습시간 랭킹 조회 - 성공")
  void testGetTotalStudyTimeRanking_Success() {
    // Given
    List<TotalStudyTime> mockTotalStudyTimes = List.of(
        TotalStudyTime.builder().studyId(1L).totalStudyTime(7200L).build(),
        TotalStudyTime.builder().studyId(2L).totalStudyTime(5400L).build()
    );

    Study mockStudy1 = Study.builder().id(1L).studyName("Java Study Group").build();
    Study mockStudy2 = Study.builder().id(2L).studyName("Python Study Group").build();

    // Mocking repository calls
    when(totalStudyTimeRepository.findTop10ByOrderByTotalStudyTimeDesc()).thenReturn(
        mockTotalStudyTimes);
    when(studyRepository.findById(1L)).thenReturn(Optional.of(mockStudy1));
    when(studyRepository.findById(2L)).thenReturn(Optional.of(mockStudy2));

    // When
    List<TotalStudyTimeDto> result = totalStudyTimeService.getTotalStudyTimeRanking();

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Java Study Group", result.get(0).getStudyName());
    assertEquals("2시간 ", result.get(0).getTotalStudyTime());
    assertEquals("Python Study Group", result.get(1).getStudyName());
    assertEquals("1시간 30분 ", result.get(1).getTotalStudyTime());

    // Verify that the mock methods were called as expected
    verify(totalStudyTimeRepository).findTop10ByOrderByTotalStudyTimeDesc();
    verify(studyRepository).findById(1L);  // Check if the repository was called correctly
    verify(studyRepository).findById(2L);
  }

  @Test
  @DisplayName("전체 스터디 누적 학습시간 랭킹 조회 - 실패 (데이터 없음)")
  void testGetTotalStudyTimeRanking_NoData() {
    // Given
    when(totalStudyTimeRepository.findTop10ByOrderByTotalStudyTimeDesc()).thenReturn(List.of());

    // When
    List<TotalStudyTimeDto> result = totalStudyTimeService.getTotalStudyTimeRanking();

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(totalStudyTimeRepository).findTop10ByOrderByTotalStudyTimeDesc();
    verify(studyRepository, never()).findById(any());
  }
}