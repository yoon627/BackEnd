package com.devonoff.domain.studyPost.dto;

import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySubject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPostUpdateRequest {

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
  @Nullable
  private MultipartFile file;
  @NotNull(message = "최대 모집 인원은 필수입니다.")
  @Min(value = 2, message = "최소 모집 인원은 2명입니다.")
  @Max(value = 10, message = "최대 모집 인원은 10명입니다.")
  private Integer maxParticipants;
  private Long userId;
  private Long studyPostId;

}
