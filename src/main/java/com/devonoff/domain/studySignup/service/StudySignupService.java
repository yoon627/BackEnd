package com.devonoff.domain.studySignup.service;

import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studySignup.dto.StudySignupCreateDto;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.studySignup.repository.StudySignupRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudySignupStatus;
import com.devonoff.type.StudyStatus;
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
  public StudySignupCreateDto.Response createStudySignup(StudySignupCreateDto.Request request) {
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

    return new StudySignupCreateDto.Response("스터디 신청이 완료되었습니다.");
  }
}