package com.devonoff.domain.study.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.type.StudyStatus;
import com.devonoff.util.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
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
  private TimeProvider timeProvider; // Mock TimeProvider

  @InjectMocks
  private StudyService studyService;

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