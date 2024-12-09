package com.devonoff.domain.study.dto;

import com.devonoff.domain.study.entity.Study;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import com.devonoff.util.DayTypeUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyDto {

  private Long id;
  private String studyName;
  private StudySubject subject;
  private StudyDifficulty difficulty;
  private List<String> dayType;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalTime startTime;
  private LocalTime endTime;
  private StudyMeetingType meetingType;
  private StudyStatus status;
  private Long studyPostId;
  private Long studyLeaderId;
  private Integer totalParticipants;

  public static StudyDto fromEntity(Study study) {
    return StudyDto.builder()
        .id(study.getId())
        .studyName(study.getStudyName())
        .subject(study.getSubject())
        .difficulty(study.getDifficulty())
        .dayType(DayTypeUtils.decodeDays(study.getDayType()))
        .startDate(study.getStartDate())
        .endDate(study.getEndDate())
        .startTime(study.getStartTime())
        .endTime(study.getEndTime())
        .meetingType(study.getMeetingType())
        .status(study.getStatus())
        .studyPostId(study.getStudyPost().getId())
        .studyLeaderId(study.getStudyLeader().getId())
        .totalParticipants(study.getTotalParticipants())
        .build();
  }
}