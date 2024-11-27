package com.devonoff.domain.studyPost.entity;

import com.devonoff.domain.studyPost.dto.StudyPostCreateDto;
import com.devonoff.entity.BaseTimeEntity;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import com.devonoff.user.entity.User;
import com.devonoff.util.DayTypeUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPost extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title; // 제목

  @Column(nullable = false)
  private String studyName; // 스터디 이름

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudySubject subject; // 주제

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudyDifficulty difficulty; // 난이드

  @Column(nullable = false)
  private Integer dayType;  // 요일 (비트 플래그 방식)

  @Column(nullable = false)
  private LocalDate startDate; // 시작일

  @Column(nullable = false)
  private LocalDate endDate; // 종료일

  @Column(nullable = false)
  private LocalTime startTime; // 시작시간

  @Column(nullable = false)
  private LocalTime endTime; // 종료시간

  @Enumerated(EnumType.STRING)
  private StudyMeetingType meetingType; // 스터디 진행 유형

  @Column(nullable = false)
  private LocalDate recruitmentPeriod; // 모집 기한

  @Column(nullable = false)
  private String description; // 본문

  @Column
  private Double latitude; // 위도

  @Column
  private Double longitude; // 경도

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudyStatus status; // 모집글 상태

  @Column(length = 255)
  private String thumbnailImgUrl; // 썸네일 이미지 URL

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 작성자

  public static StudyPost createFromRequest(StudyPostCreateDto.Request request, User user) {
    int dayType = DayTypeUtils.encodeDaysFromRequest(request.getDayType());

    return StudyPost.builder()
        .title(request.getTitle())
        .studyName(request.getStudyName())
        .subject(request.getSubject())
        .difficulty(request.getDifficulty())
        .dayType(dayType)
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .startTime(request.getStartTime())
        .endTime(request.getEndTime())
        .meetingType(request.getMeetingType())
        .recruitmentPeriod(request.getRecruitmentPeriod())
        .description(request.getDescription())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .status(StudyStatus.RECRUITING) // 기본값 설정
        .thumbnailImgUrl(request.getThumbnailImgUrl())
        .user(user)
        .build();
  }
}
