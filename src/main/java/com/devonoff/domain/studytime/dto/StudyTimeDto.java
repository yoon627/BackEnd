package com.devonoff.domain.studytime.dto;

import com.devonoff.domain.studytime.entity.StudyTime;
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
public class StudyTimeDto {

  // TODO 프론트엔드로 보낼때 필요한 것들 추가해야함
  private Long studyId;
  private String studyName;
  private LocalDateTime startedAt;
  private LocalDateTime endedAt;

  public static StudyTimeDto fromEntity(StudyTime studyTime) {
    return StudyTimeDto.builder()
        .studyId(studyTime.getStudyId())
        .startedAt(studyTime.getStartedAt())
        .endedAt(studyTime.getEndedAt())
        .build();
  }

  public static StudyTimeDto fromEntityWithStudyName(StudyTime studyTime, String studyName) {
    return StudyTimeDto.builder()
        .studyId(studyTime.getStudyId())
        .studyName(studyName)
        .startedAt(studyTime.getStartedAt())
        .endedAt(studyTime.getEndedAt())
        .build();
  }
}
