package com.devonoff.domain.totalstudytime.service;

import static com.devonoff.type.ErrorCode.STUDY_NOT_FOUND;

import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.totalstudytime.dto.TotalStudyTimeDto;
import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
import com.devonoff.exception.CustomException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
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

    List<TotalStudyTime> allTotalStudyTimes = this.totalStudyTimeRepository.findAllByOrderByTotalStudyTimeDesc();

    AtomicLong rank = new AtomicLong(1);
    Long studyRanking = allTotalStudyTimes.stream()
        .map(entity -> {
          if (entity.getStudyId().equals(studyId)) {
            return rank.get();
          }
          rank.incrementAndGet();
          return null;
        })
        .filter(java.util.Objects::nonNull) // Null 값 제외
        .findFirst()
        .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

    return TotalStudyTimeDto.fromEntityWithStudyNameAndRanking(
        this.totalStudyTimeRepository.findById(studyId).orElseThrow(() -> new CustomException(
            STUDY_NOT_FOUND)), studyName, studyRanking,
        (studyRanking.doubleValue() / allTotalStudyTimes.size()) * 100);
  }

  public List<TotalStudyTimeDto> getTotalStudyTimeRanking() {
    Long totalStudyTimeCnt = this.totalStudyTimeRepository.count();
    AtomicLong rankingCount = new AtomicLong(1);
    return this.totalStudyTimeRepository.findTop10ByOrderByTotalStudyTimeDesc().stream()
        .map(totalStudyTime -> {
          String studyName = this.studyRepository.findById(totalStudyTime.getStudyId())
              .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND)).getStudyName();
          return TotalStudyTimeDto.fromEntityWithStudyNameAndRanking(totalStudyTime, studyName,
              rankingCount.get(),
              (rankingCount.getAndIncrement() / totalStudyTimeCnt.doubleValue()) * 100);
        }).collect(Collectors.toList());
  }
}
