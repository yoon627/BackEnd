package com.devonoff.domain.study.entity;

import com.devonoff.common.entity.BaseTimeEntity;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudySubject;
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
import jakarta.persistence.OneToOne;
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
public class Study extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String studyName; // 스터디 이름

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudySubject subject; // 주제

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudyDifficulty difficulty; // 난이도

  @Column(nullable = false)
  private Integer dayType; // 요일

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

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_post_id", nullable = false)
  private StudyPost studyPost; // 스터디 모집글

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User studyLeader; // 스터디장
}
