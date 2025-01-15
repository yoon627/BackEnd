package com.devonoff.domain.studySignup.service;

import com.devonoff.domain.notification.dto.NotificationDto;
import com.devonoff.domain.notification.service.NotificationService;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studySignup.dto.StudySignupCreateRequest;
import com.devonoff.domain.studySignup.dto.StudySignupDto;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.studySignup.repository.StudySignupRepository;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.NotificationType;
import com.devonoff.type.PostType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySignupStatus;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudySignupService {

  private final StudySignupRepository studySignupRepository;
  private final StudyPostRepository studyPostRepository;
  private final UserRepository userRepository;
  private final AuthService authService;
  private final NotificationService notificationService;

  // 스터디 신청
  public StudySignupDto createStudySignup(StudySignupCreateRequest request) {
    validateOwnership(request.getUserId(), authService.getLoginUserId());

    StudyPost studyPost = findStudyPostById(request.getStudyPostId());
    validateRecruitingStatus(studyPost);

    User user = findUserById(request.getUserId());
    checkDuplicateSignup(studyPost, user);

    StudySignup studySignup = StudySignup.builder()
        .studyPost(studyPost)
        .user(user)
        .status(StudySignupStatus.PENDING) // 기본값: 대기
        .build();

    studySignupRepository.save(studySignup);
    notificationService.sendNotificationToUser(studyPost.getUser().getId(),
        NotificationDto.builder()
            .type(NotificationType.STUDY_SIGNUP_ADDED)
            .userId(studyPost.getUser().getId())
            .sender(UserDto.fromEntity(user))
            .postType(PostType.STUDY)
            .postTitle(studyPost.getTitle())
            .postContent(studyPost.getDescription())
            .studyName(studyPost.getStudyName())
            .targetId(studyPost.getId())
            .isRead(false)
            .build());
    return StudySignupDto.fromEntity(studySignup);
  }

  // 신청 상태 관리(승인/거절)
  public void updateSignupStatus(Long studySignupId, StudySignupStatus newStatus) {
    StudySignup studySignup = findSignupById(studySignupId);
    StudyPost studyPost = studySignup.getStudyPost();

    validateOwnership(studyPost.getUser().getId(), authService.getLoginUserId());
    validateRecruitingStatus(studyPost);

    processSignupStatusChange(studySignup, studyPost, newStatus);
    notificationService.sendNotificationToUser(studySignup.getUser().getId(),
        NotificationDto.builder()
            .type(newStatus == StudySignupStatus.APPROVED ? NotificationType.STUDY_SIGNUP_APPROVED
                : NotificationType.STUDY_SIGNUP_REJECTED)
            .userId(studySignup.getUser().getId())
            .sender(UserDto.fromEntity(studySignup.getStudyPost().getUser()))
            .postType(PostType.STUDY)
            .postTitle(studyPost.getTitle())
            .postContent(studyPost.getDescription())
            .studyName(studyPost.getStudyName())
            .targetId(studyPost.getId())
            .build());
    studySignupRepository.save(studySignup);
    studyPostRepository.save(studyPost);
  }

  // 신청 목록 조회
  public List<StudySignupDto> getSignupList(Long studyPostId, StudySignupStatus status) {
    StudyPost studyPost = findStudyPostById(studyPostId);

    // 상태별 신청 목록 조회
    List<StudySignup> studySignups;
    if (status != null) {
      studySignups = studySignupRepository.findByStudyPostAndStatus(studyPost, status);
    } else {
      studySignups = studySignupRepository.findByStudyPost(studyPost);
    }

    return studySignups.stream()
        .map(StudySignupDto::fromEntity)
        .collect(Collectors.toList());
  }

  // 신청 취소
  public void cancelSignup(Long studySignupId) {
    StudySignup studySignup = findSignupById(studySignupId);
    validateOwnership(studySignup.getUser().getId(), authService.getLoginUserId());

    if (studySignup.getStatus() == StudySignupStatus.APPROVED) {
      StudyPost studyPost = studySignup.getStudyPost();
      studyPost.decrementParticipants();
      studyPostRepository.save(studyPost);
    }

    studySignupRepository.delete(studySignup);
  }

  // ================================= Helper methods ================================= //

  private StudyPost findStudyPostById(Long id) {
    return studyPostRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));
  }

  private StudySignup findSignupById(Long id) {
    return studySignupRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.SIGNUP_NOT_FOUND));
  }

  private User findUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  private void validateOwnership(Long ownerId, Long loggedInUserId) {
    if (!ownerId.equals(loggedInUserId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }
  }

  private void validateRecruitingStatus(StudyPost studyPost) {
    if (studyPost.getStatus() != StudyPostStatus.RECRUITING) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }
  }

  private void checkDuplicateSignup(StudyPost studyPost, User user) {
    if (studySignupRepository.existsByStudyPostAndUser(studyPost, user)) {
      throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
    }
  }

  // ================= 신청 상태 관리(승인/거절) 분리 메서드 =================

  // 신청 상태 변경 처리
  private void processSignupStatusChange(StudySignup studySignup, StudyPost studyPost,
      StudySignupStatus newStatus) {
    if (isPendingToApproved(studySignup, newStatus)) {
      approveSignup(studySignup, studyPost);
    } else if (isApprovedToRejected(studySignup, newStatus)) {
      rejectApprovedSignup(studySignup, studyPost);
    } else if (isPendingToRejected(studySignup, newStatus)) {
      rejectPendingSignup(studySignup);
    } else {
      throw new CustomException(ErrorCode.SIGNUP_STATUS_ALREADY_FINALIZED);
    }
  }

  // 상태 변경 조건
  private boolean isPendingToApproved(StudySignup studySignup, StudySignupStatus newStatus) {
    return studySignup.getStatus() == StudySignupStatus.PENDING
        && newStatus == StudySignupStatus.APPROVED;
  }

  // 상태 변경 조건
  private boolean isApprovedToRejected(StudySignup studySignup, StudySignupStatus newStatus) {
    return studySignup.getStatus() == StudySignupStatus.APPROVED
        && newStatus == StudySignupStatus.REJECTED;
  }

  // 상태 변경 조건
  private boolean isPendingToRejected(StudySignup studySignup, StudySignupStatus newStatus) {
    return studySignup.getStatus() == StudySignupStatus.PENDING
        && newStatus == StudySignupStatus.REJECTED;
  }

  // 승인 처리
  private void approveSignup(StudySignup studySignup, StudyPost studyPost) {
    if (studyPost.isFull()) {
      throw new CustomException(ErrorCode.STUDY_POST_FULL);
    }
    studySignup.setStatus(StudySignupStatus.APPROVED);
    studyPost.incrementParticipants();
  }

  // 승인된 신청 거절 처리
  private void rejectApprovedSignup(StudySignup studySignup, StudyPost studyPost) {
    studySignup.setStatus(StudySignupStatus.REJECTED);
    studyPost.decrementParticipants();
  }

  // 대기 상태 신청 거절 처리
  private void rejectPendingSignup(StudySignup studySignup) {
    studySignup.setStatus(StudySignupStatus.REJECTED);
  }
}