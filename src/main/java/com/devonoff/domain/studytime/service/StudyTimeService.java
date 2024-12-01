package com.devonoff.domain.studytime.service;

import static com.devonoff.type.ErrorCode.STUDY_NOT_FOUND;

import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.studytime.dto.StudyTimeDto;
import com.devonoff.domain.studytime.entity.StudyTime;
import com.devonoff.domain.studytime.repository.StudyTimeRepository;
import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
import com.devonoff.exception.CustomException;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyTimeService {

  private final StudyTimeRepository studyTimeRepository;
  private final TotalStudyTimeRepository totalStudyTimeRepository;
  private final StudyRepository studyRepository;

  public List<StudyTimeDto> findAllStudyTimes(Long studyId) {
    return this.studyTimeRepository.findAllByStudyIdAndEndedAtIsNotNull(studyId).stream()
        .map(studyTime -> {
          Study study = this.studyRepository.findById(studyId)
              .orElseThrow(() -> new CustomException(
                  STUDY_NOT_FOUND));
          return StudyTimeDto.fromEntityWithStudyName(studyTime, study.getStudyName());
        })
        .collect(
            Collectors.toList());
  }

  //TODO 화상채팅과 연동시 작동하도록 확인
  @Transactional
  public StudyTime saveStudyTime(Long studyId, LocalDateTime startedAt, LocalDateTime endedAt) {
    TotalStudyTime totalStudyTime = this.totalStudyTimeRepository.findById(studyId)
        .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
    Long totalSeconds = totalStudyTime.getTotalStudyTime();
    totalSeconds += Duration.between(startedAt, endedAt).toSeconds();
    totalStudyTime.setTotalStudyTime(totalSeconds);
    this.totalStudyTimeRepository.save(totalStudyTime);
    return this.studyTimeRepository.save(
        StudyTime.builder().studyId(studyId).startedAt(startedAt).endedAt(endedAt)
            .build());
  }
}
