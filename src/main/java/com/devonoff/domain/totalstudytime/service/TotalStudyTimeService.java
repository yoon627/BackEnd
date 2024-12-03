package com.devonoff.domain.totalstudytime.service;

import static com.devonoff.type.ErrorCode.STUDY_NOT_FOUND;

import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.totalstudytime.dto.TotalStudyTimeDto;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
import com.devonoff.exception.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotalStudyTimeService {

  private final TotalStudyTimeRepository totalStudyTimeRepository;
  private final StudyRepository studyRepository;

  public TotalStudyTimeDto getTotalStudyTime(Long studyId) {
    String studyName = this.studyRepository.findById(studyId)
        .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND)).getStudyName();
    return TotalStudyTimeDto.fromEntityWithStudyName(
        this.totalStudyTimeRepository.findById(studyId).orElseThrow(() -> new CustomException(
            STUDY_NOT_FOUND)), studyName);
  }

  public List<TotalStudyTimeDto> getTotalStudyTimeRanking() {
    return this.totalStudyTimeRepository.findTop10ByOrderByTotalStudyTimeDesc().stream()
        .map(totalStudyTime -> {
          String studyName = this.studyRepository.findById(totalStudyTime.getId())
              .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND)).getStudyName();
          return TotalStudyTimeDto.fromEntityWithStudyName(totalStudyTime, studyName);
        }).collect(Collectors.toList());
  }
}
