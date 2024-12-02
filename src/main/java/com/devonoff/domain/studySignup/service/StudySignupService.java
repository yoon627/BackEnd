package com.devonoff.domain.studySignup.service;

import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studySignup.dto.StudySignupCreateRequest;
import com.devonoff.domain.studySignup.dto.StudySignupCreateResponse;
import com.devonoff.domain.studySignup.dto.StudySignupDto;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.studySignup.repository.StudySignupRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudySignupStatus;
import com.devonoff.type.StudyStatus;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudySignupService {

  private final StudySignupRepository studySignupRepository;
  private final StudyPostRepository studyPostRepository;
  private final UserRepository userRepository;

  // 스터디 신청
  @Transactional
  public StudySignupCreateResponse createStudySignup(StudySignupCreateRequest request) {
    StudyPost studyPost = studyPostRepository.findById(request.getStudyPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    if (studyPost.getStatus() != StudyStatus.RECRUITING) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    boolean alreadySignedUp = studySignupRepository.existsByStudyPostAndUser(studyPost, user);
    if (alreadySignedUp) {
      throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
    }

    StudySignup studySignup = StudySignup.builder()
        .studyPost(studyPost)
        .user(user)
        .status(StudySignupStatus.PENDING) // 기본값: 대기
        .build();

    studySignupRepository.save(studySignup);

    return new StudySignupCreateResponse("스터디 신청이 완료되었습니다.");
  }

  // 신청 상태 관리(승인/거절)
  public void updateSignupStatus(Long studySignupId, StudySignupStatus newStatus) {
    StudySignup studySignup = studySignupRepository.findById(studySignupId)
        .orElseThrow(() -> new CustomException(ErrorCode.SIGNUP_NOT_FOUND));

    StudyPost studyPost = studySignup.getStudyPost();

    if (studyPost.getStatus() != StudyStatus.RECRUITING) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    if (studySignup.getStatus() == StudySignupStatus.PENDING
        && newStatus == StudySignupStatus.APPROVED) {
      if (studyPost.isFull()) {
        throw new CustomException(ErrorCode.STUDY_POST_FULL);
      }
      studyPost.incrementParticipants();
    } else if (studySignup.getStatus() == StudySignupStatus.APPROVED
        && newStatus == StudySignupStatus.REJECTED) {
      studyPost.decrementParticipants();
    } else if (studySignup.getStatus() == StudySignupStatus.PENDING
        && newStatus == StudySignupStatus.REJECTED) {
      studySignup.setStatus(StudySignupStatus.REJECTED);
    } else {
      throw new CustomException(ErrorCode.SIGNUP_STATUS_ALREADY_FINALIZED);
    }

    studySignup.setStatus(newStatus);
    studyPostRepository.save(studyPost);
    studySignupRepository.save(studySignup);
    // TODO: 리펙토링 예정 (메서드 분리)
  }

  // 신청 목록 조회
  public List<StudySignupDto> getSignupList(Long studyPostId, StudySignupStatus status) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

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
  public void cancelSignup(Long studySignupId, Long userId) {
    StudySignup studySignup = studySignupRepository.findById(studySignupId)
        .orElseThrow(() -> new CustomException(ErrorCode.SIGNUP_NOT_FOUND));

    if (!studySignup.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    if (studySignup.getStatus() == StudySignupStatus.APPROVED) {
      StudyPost studyPost = studySignup.getStudyPost();
      studyPost.decrementParticipants();
      studyPostRepository.save(studyPost);
    }

    studySignupRepository.delete(studySignup);
  }
}