package com.devonoff.studyPost.dto;

import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class StudyPostUpdateDto {

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

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
    private StudyStatus status;
    private String thumbnailImgUrl;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Response {

    private String message;
  }
}
