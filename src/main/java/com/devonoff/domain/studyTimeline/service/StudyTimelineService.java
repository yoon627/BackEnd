package com.devonoff.domain.studyTimeline.service;

import static com.devonoff.type.ErrorCode.STUDY_NOT_FOUND;

import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.studyTimeline.dto.StudyTimelineDto;
import com.devonoff.domain.studyTimeline.entity.StudyTimeline;
import com.devonoff.domain.studyTimeline.repository.StudyTimelineRepository;
import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
import com.devonoff.exception.CustomException;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyTimelineService {

  private final StudyTimelineRepository studyTimelineRepository;
  private final TotalStudyTimeRepository totalStudyTimeRepository;
  private final StudyRepository studyRepository;

  public List<StudyTimelineDto> findAllStudyTimelines(Long studyId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    Long userId = Long.parseLong(userDetails.getUsername());
    //TODO userId와 student 연동해서 로그인된 유저가 해당 스터디에 속하는지 확인해야함
    String studyName = studyRepository.findById(studyId)
        .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND)).getStudyName();
    return this.studyTimelineRepository.findAllByStudyIdAndEndedAtIsNotNull(studyId).stream()
        .map(studyTimeline -> {
          return StudyTimelineDto.fromEntityWithStudyName(studyTimeline, studyName);
        })
        .collect(
            Collectors.toList());
  }

  //TODO 화상채팅과 연동시 작동하도록 확인
  @Transactional
  public StudyTimeline saveStudyTimeline(Long studyId, LocalDateTime startedAt,
      LocalDateTime endedAt) {
    TotalStudyTime totalStudyTime = this.totalStudyTimeRepository.findById(studyId)
        .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
    Long totalSeconds = totalStudyTime.getTotalStudyTime();
    totalSeconds += Duration.between(startedAt, endedAt).toSeconds();
    totalStudyTime.setTotalStudyTime(totalSeconds);
    this.totalStudyTimeRepository.save(totalStudyTime);
    return this.studyTimelineRepository.save(
        StudyTimeline.builder().studyId(studyId).startedAt(startedAt).endedAt(endedAt)
            .build());
  }
}
