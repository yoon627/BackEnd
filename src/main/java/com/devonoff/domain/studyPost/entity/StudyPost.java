package com.devonoff.domain.studyPost.entity;

import com.devonoff.common.entity.BaseTimeEntity;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.studyPost.dto.StudyPostUpdateRequest;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.user.entity.User;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySubject;
import com.devonoff.util.DayTypeUtils;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
  private StudyDifficulty difficulty; // 난이도

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
  @Column(nullable = false)
  private StudyMeetingType meetingType; // 스터디 진행 유형

  @Column(nullable = false)
  private LocalDate recruitmentPeriod; // 모집 기한

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description; // 본문

  @Column
  private Double latitude; // 위도

  @Column
  private Double longitude; // 경도

  @Column
  private String address; // 주소 정보

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudyPostStatus status; // 모집글 상태

  @Column
  private String thumbnailImgUrl; // 썸네일 이미지 URL

  @Column(nullable = false)
  private Integer maxParticipants; // 모집 최대 인원

  @Column(nullable = false)
  private Integer currentParticipants; // 현재 승인된 참가자 수

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 작성자

  @OneToMany(mappedBy = "studyPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<StudySignup> signups;

  @OneToMany(mappedBy = "studyPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Study> studies;

  public void cancelRecruitment() {
    this.status = StudyPostStatus.CANCELED;
    this.setUpdatedAt(LocalDateTime.now()); // 수동으로 updatedAt 갱신
  }

  public void closeRecruitment() {
    this.status = StudyPostStatus.CLOSED;
  }

  public void updateFields(StudyPostUpdateRequest request, String thumbnailImgUrl) {
    validateDates(request.getStartDate(), request.getEndDate());
    validateTimes(request.getStartTime(), request.getEndTime());
    this.title = request.getTitle();
    this.studyName = request.getStudyName();
    this.subject = request.getSubject();
    this.difficulty = request.getDifficulty();
    this.dayType = DayTypeUtils.encodeDaysFromRequest(request.getDayType());
    this.startDate = request.getStartDate();
    this.endDate = request.getEndDate();
    this.startTime = request.getStartTime();
    this.endTime = request.getEndTime();
    this.meetingType = request.getMeetingType();
    this.recruitmentPeriod = request.getRecruitmentPeriod();
    this.description = request.getDescription();
    this.latitude = request.getLatitude();
    this.longitude = request.getLongitude();
    this.address = request.getAddress();
    this.status = request.getStatus();
    this.thumbnailImgUrl = thumbnailImgUrl;
    this.maxParticipants = request.getMaxParticipants();
  }

  public boolean isFull() {
    return currentParticipants >= (maxParticipants - 1); // 스터디장 제외
  }

  public void incrementParticipants() {
    if (isFull()) {
      throw new CustomException(ErrorCode.STUDY_POST_FULL);
    }
    this.currentParticipants++;
  }

  public void decrementParticipants() {
    if (currentParticipants > 0) {
      this.currentParticipants--;
    }
  }

  private void validateDates(LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
    }
  }

  private void validateTimes(LocalTime startTime, LocalTime endTime) {
    if (startTime.isAfter(endTime)) {
      throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
    }
  }
}