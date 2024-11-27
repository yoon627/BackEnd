package com.devonoff.domain.studyPost.dto;

import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

public class StudyPostCreateDto {

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "스터디 이름은 필수입니다.")
    private String studyName;

    @NotNull(message = "주제는 필수입니다.")
    private StudySubject subject;

    @NotNull(message = "난이도는 필수입니다.")
    private StudyDifficulty difficulty;

    @NotNull(message = "요일 정보는 필수입니다.")
    private List<String> dayType;

    @NotNull(message = "스터디 시작일은 필수입니다.")
    @FutureOrPresent(message = "스터디 시작일은 과거일 수 없습니다.")
    private LocalDate startDate;

    @NotNull(message = "스터디 종료일은 필수입니다.")
    @Future(message = "스터디 종료일은 오늘 이후여야 합니다.")
    private LocalDate endDate;

    @NotNull(message = "스터디 시작 시간은 필수입니다.")
    private LocalTime startTime;

    @NotNull(message = "스터디 종료 시간은 필수입니다.")
    private LocalTime endTime;

    @NotNull(message = "스터디 진행 유형은 필수입니다.")
    private StudyMeetingType meetingType;

    @NotNull(message = "모집 기한은 필수입니다.")
    private LocalDate recruitmentPeriod;

    @NotBlank(message = "본문 내용은 필수입니다.")
    private String description;

    @Nullable
    private Double latitude;

    @Nullable
    private Double longitude;

    private StudyStatus status = StudyStatus.RECRUITING; // 기본값: 모집 중

    @Nullable
    private String thumbnailImgUrl; // 썸네일 이미지

    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long userId;
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
