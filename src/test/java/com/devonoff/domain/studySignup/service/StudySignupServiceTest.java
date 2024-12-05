package com.devonoff.domain.studySignup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studySignup.dto.StudySignupCreateRequest;
import com.devonoff.domain.studySignup.dto.StudySignupDto;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.studySignup.repository.StudySignupRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySignupStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudySignupServiceTest {

  @InjectMocks
  private StudySignupService studySignupService;

  @Mock
  private StudyPostRepository studyPostRepository;

  @Mock
  private StudySignupRepository studySignupRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthService authService;

  @Test
  @DisplayName("스터디 신청 성공")
  void createStudySignup_Success() {
    // Given
    Long userId = 1L;
    Long studyPostId = 100L;
    Long loggedInUserId = 1L;

    StudyPost studyPost = StudyPost.builder()
        .id(studyPostId)
        .status(StudyPostStatus.RECRUITING)
        .build();

    User user = User.builder()
        .id(userId)
        .nickname("참가자")
        .build();

    when(authService.getLoginUserId()).thenReturn(loggedInUserId);
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(studySignupRepository.existsByStudyPostAndUser(studyPost, user)).thenReturn(false);
    when(studySignupRepository.save(any(StudySignup.class)))
        .thenAnswer(invocation -> {
          StudySignup savedSignup = invocation.getArgument(0);
          savedSignup.setId(10L);
          return savedSignup;
        });

    // When
    StudySignupDto result = studySignupService.createStudySignup(
        new StudySignupCreateRequest(studyPostId, userId)
    );

    // Then
    assertNotNull(result);
    assertEquals(10L, result.getSignupId());
    assertEquals(userId, result.getUserId());
    assertEquals("참가자", result.getNickName());
    assertEquals(StudySignupStatus.PENDING, result.getStatus());
  }

  @Test
  @DisplayName("스터디 신청 실패 - 모집글 없음")
  void createStudySignup_Fail_StudyPostNotFound() {
    // Given
    Long studyPostId = 100L;
    Long userId = 1L;
    Long loggedInUserId = 1L;

    when(authService.getLoginUserId()).thenReturn(loggedInUserId);
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.createStudySignup(new StudySignupCreateRequest(studyPostId, userId))
    );

    assertEquals(ErrorCode.STUDY_POST_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("스터디 신청 실패 - 모집글이 모집 중이 아님")
  void createStudySignup_Fail_InvalidStudyStatus() {
    // Given
    Long studyPostId = 100L;
    Long userId = 1L;
    Long loggedInUserId = 1L;

    StudyPost studyPost = StudyPost.builder()
        .id(studyPostId)
        .status(StudyPostStatus.CLOSED) // 모집 종료 상태
        .build();

    when(authService.getLoginUserId()).thenReturn(loggedInUserId);
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.createStudySignup(new StudySignupCreateRequest(studyPostId, userId))
    );

    assertEquals(ErrorCode.INVALID_STUDY_STATUS, exception.getErrorCode());
  }

  @Test
  @DisplayName("스터디 신청 실패 - 유저 없음")
  void createStudySignup_Fail_UserNotFound() {
    // Given
    Long studyPostId = 100L;
    Long userId = 1L;
    Long loggedInUserId = 1L;

    StudyPost studyPost = StudyPost.builder()
        .id(studyPostId)
        .status(StudyPostStatus.RECRUITING)
        .build();

    when(authService.getLoginUserId()).thenReturn(loggedInUserId);
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.createStudySignup(new StudySignupCreateRequest(studyPostId, userId))
    );

    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("스터디 신청 실패 - 이미 해당 스터디에 신청함")
  void createStudySignup_Fail_DuplicateApplication() {
    // Given
    Long studyPostId = 100L;
    Long userId = 1L;
    Long loggedInUserId = 1L;

    StudyPost studyPost = StudyPost.builder()
        .id(studyPostId)
        .status(StudyPostStatus.RECRUITING)
        .build();

    User user = User.builder()
        .id(userId)
        .build();

    when(authService.getLoginUserId()).thenReturn(loggedInUserId);
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(studySignupRepository.existsByStudyPostAndUser(studyPost, user)).thenReturn(true);

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.createStudySignup(new StudySignupCreateRequest(studyPostId, userId))
    );

    assertEquals(ErrorCode.DUPLICATE_APPLICATION, exception.getErrorCode());
  }

}
