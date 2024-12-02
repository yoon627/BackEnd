package com.devonoff.domain.studyTimeline.dto;

import com.devonoff.domain.studyTimeline.entity.StudyTimeline;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyTimelineDto {

  // TODO 프론트엔드로 보낼때 필요한 것들 추가해야함
  private Long studyId;
  private String studyName;
  private LocalDateTime startedAt;
  private LocalDateTime endedAt;

  public static StudyTimelineDto fromEntity(StudyTimeline studyTime) {
    return StudyTimelineDto.builder()
        .studyId(studyTime.getStudyId())
        .startedAt(studyTime.getStartedAt())
        .endedAt(studyTime.getEndedAt())
        .build();
  }

  public static StudyTimelineDto fromEntityWithStudyName(StudyTimeline studyTime,
      String studyName) {
    return StudyTimelineDto.builder()
        .studyId(studyTime.getStudyId())
        .studyName(studyName)
        .startedAt(studyTime.getStartedAt())
        .endedAt(studyTime.getEndedAt())
        .build();
  }
}
