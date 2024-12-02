package com.devonoff.domain.studySignup.entity;

import com.devonoff.common.entity.BaseTimeEntity;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.StudySignupStatus;
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
public class StudySignup extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudySignupStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 신청자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_post_id", nullable = false)
  private StudyPost studyPost; // 스터디 모집글
}