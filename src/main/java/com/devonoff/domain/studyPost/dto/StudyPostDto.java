package com.devonoff.domain.studyPost.dto;


import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySubject;
import com.devonoff.util.DayTypeUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPostDto {

  private Long id;
  private String title;
  private String studyName;
  private StudySubject subject;
  private StudyDifficulty difficulty;
  private List<String> dayType;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalTime startTime;
  private LocalTime endTime;
  private StudyMeetingType meetingType;
  private LocalDate recruitmentPeriod;
  private String description;
  private Double latitude;
  private Double longitude;
  private StudyPostStatus status;
  private String thumbnailImgUrl;
  private Integer maxParticipants;
  private Integer currentParticipants;
  private Long userId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static StudyPostDto fromEntity(StudyPost studyPost) {
    return StudyPostDto.builder()
        .id(studyPost.getId())
        .title(studyPost.getTitle())
        .studyName(studyPost.getStudyName())
        .subject(studyPost.getSubject())
        .difficulty(studyPost.getDifficulty())
        .dayType(DayTypeUtils.decodeDays(studyPost.getDayType()))
        .startDate(studyPost.getStartDate())
        .endDate(studyPost.getEndDate())
        .startTime(studyPost.getStartTime())
        .endTime(studyPost.getEndTime())
        .meetingType(studyPost.getMeetingType())
        .recruitmentPeriod(studyPost.getRecruitmentPeriod())
        .description(studyPost.getDescription())
        .latitude(studyPost.getLatitude())
        .longitude(studyPost.getLongitude())
        .status(studyPost.getStatus())
        .thumbnailImgUrl(studyPost.getThumbnailImgUrl())
        .maxParticipants(studyPost.getMaxParticipants())
        .currentParticipants(studyPost.getCurrentParticipants())
        .userId(studyPost.getUser().getId())
        .createdAt(studyPost.getCreatedAt())
        .updatedAt(studyPost.getUpdatedAt())
        .build();
  }
}