package com.devonoff.domain.studySignup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import java.util.List;
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

  @Test
  @DisplayName("신청 상태 관리 성공 - 승인")
  void updateSignupStatus_Approve_Success() {
    // Given
    Long studySignupId = 1L;
    Long loggedInUserId = 100L;

    StudyPost studyPost = StudyPost.builder()
        .id(10L)
        .status(StudyPostStatus.RECRUITING)
        .currentParticipants(2)
        .maxParticipants(5)
        .user(User.builder().id(loggedInUserId).build())
        .build();

    StudySignup studySignup = StudySignup.builder()
        .id(studySignupId)
        .status(StudySignupStatus.PENDING)
        .studyPost(studyPost)
        .build();

    when(studySignupRepository.findById(studySignupId)).thenReturn(Optional.of(studySignup));
    when(authService.getLoginUserId()).thenReturn(loggedInUserId);

    // When
    studySignupService.updateSignupStatus(studySignupId, StudySignupStatus.APPROVED);

    // Then
    assertEquals(StudySignupStatus.APPROVED, studySignup.getStatus());
    assertEquals(3, studyPost.getCurrentParticipants());

    verify(studySignupRepository).save(studySignup);
    verify(studyPostRepository).save(studyPost);
  }

  @Test
  @DisplayName("신청 상태 관리 성공 - 취소")
  void updateSignupStatus_Reject_Success() {
    // Given
    Long studySignupId = 1L;
    Long loggedInUserId = 100L;

    StudyPost studyPost = StudyPost.builder()
        .id(10L)
        .status(StudyPostStatus.RECRUITING)
        .currentParticipants(3)
        .maxParticipants(5)
        .user(User.builder().id(loggedInUserId).build())
        .build();

    StudySignup studySignup = StudySignup.builder()
        .id(studySignupId)
        .status(StudySignupStatus.PENDING)
        .studyPost(studyPost)
        .build();

    when(studySignupRepository.findById(studySignupId)).thenReturn(Optional.of(studySignup));
    when(authService.getLoginUserId()).thenReturn(loggedInUserId);

    // When
    studySignupService.updateSignupStatus(studySignupId, StudySignupStatus.REJECTED);

    // Then
    assertEquals(StudySignupStatus.REJECTED, studySignup.getStatus());
    assertEquals(3, studyPost.getCurrentParticipants());

    verify(studySignupRepository).save(studySignup);
    verify(studyPostRepository).save(studyPost);
  }

  @Test
  @DisplayName("신청 상태 관리 실패 - 신청 내역 없음")
  void updateSignupStatus_Fail_SignupNotFound() {
    // Given
    Long studySignupId = 1L;

    when(studySignupRepository.findById(studySignupId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.updateSignupStatus(studySignupId, StudySignupStatus.APPROVED)
    );

    assertEquals(ErrorCode.SIGNUP_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("신청 상태 관리 실패 - 모집 상태가 모집 중이 아님")
  void updateSignupStatus_Fail_InvalidStudyStatus() {
    // Given
    Long studySignupId = 1L;
    Long loggedInUserId = 100L;

    StudyPost studyPost = StudyPost.builder()
        .id(10L)
        .status(StudyPostStatus.CLOSED)
        .user(User.builder().id(loggedInUserId).build())
        .build();

    StudySignup studySignup = StudySignup.builder()
        .id(studySignupId)
        .status(StudySignupStatus.PENDING)
        .studyPost(studyPost)
        .build();

    when(studySignupRepository.findById(studySignupId)).thenReturn(Optional.of(studySignup));
    when(authService.getLoginUserId()).thenReturn(loggedInUserId);

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.updateSignupStatus(studySignupId, StudySignupStatus.APPROVED)
    );

    assertEquals(ErrorCode.INVALID_STUDY_STATUS, exception.getErrorCode());
  }

  @Test
  @DisplayName("신청 상태 관리 실패 - 이미 확정된 상태")
  void updateSignupStatus_Fail_StatusAlreadyFinalized() {
    // Given
    Long studySignupId = 1L;
    Long loggedInUserId = 100L;

    StudyPost studyPost = StudyPost.builder()
        .id(10L)
        .status(StudyPostStatus.RECRUITING)
        .currentParticipants(3)
        .maxParticipants(5)
        .user(User.builder().id(loggedInUserId).build())
        .build();

    StudySignup studySignup = StudySignup.builder()
        .id(studySignupId)
        .status(StudySignupStatus.APPROVED) // 이미 확정된 상태
        .studyPost(studyPost)
        .build();

    when(studySignupRepository.findById(studySignupId)).thenReturn(Optional.of(studySignup));
    when(authService.getLoginUserId()).thenReturn(loggedInUserId);

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.updateSignupStatus(studySignupId, StudySignupStatus.APPROVED)
    );

    assertEquals(ErrorCode.SIGNUP_STATUS_ALREADY_FINALIZED, exception.getErrorCode());
  }

  @Test
  @DisplayName("신청 목록 조회 성공")
  void getSignupList_Success() {
    // Given
    Long studyPostId = 1L;
    Long loggedInUserId = 100L;
    StudySignupStatus filterStatus = StudySignupStatus.PENDING;

    StudyPost studyPost = StudyPost.builder()
        .id(studyPostId)
        .status(StudyPostStatus.RECRUITING)
        .user(User.builder().id(loggedInUserId).build())
        .build();

    StudySignup signup1 = StudySignup.builder()
        .id(10L)
        .user(User.builder().id(101L).nickname("참가자1").build())
        .status(StudySignupStatus.PENDING)
        .studyPost(studyPost)
        .build();

    StudySignup signup2 = StudySignup.builder()
        .id(11L)
        .user(User.builder().id(102L).nickname("참가자2").build())
        .status(StudySignupStatus.PENDING)
        .studyPost(studyPost)
        .build();

    List<StudySignup> studySignups = List.of(signup1, signup2);

    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));
    when(authService.getLoginUserId()).thenReturn(loggedInUserId);
    when(studySignupRepository.findByStudyPostAndStatus(studyPost, filterStatus)).thenReturn(
        studySignups);

    // When
    List<StudySignupDto> result = studySignupService.getSignupList(studyPostId, filterStatus);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("참가자1", result.get(0).getNickName());
    assertEquals("참가자2", result.get(1).getNickName());
    verify(studyPostRepository).findById(studyPostId);
    verify(authService).getLoginUserId();
    verify(studySignupRepository).findByStudyPostAndStatus(studyPost, filterStatus);
  }

  @Test
  @DisplayName("신청 목록 조회 실패 - 모집글 없음")
  void getSignupList_Fail_StudyPostNotFound() {
    // Given
    Long studyPostId = 1L;
    StudySignupStatus filterStatus = StudySignupStatus.PENDING;

    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.getSignupList(studyPostId, filterStatus)
    );

    assertEquals(ErrorCode.STUDY_POST_NOT_FOUND, exception.getErrorCode());
    verify(studyPostRepository).findById(studyPostId);
    verifyNoInteractions(authService, studySignupRepository);
  }

  @Test
  @DisplayName("신청 취소 성공")
  void cancelSignup_Success() {
    // Given
    Long studySignupId = 1L;
    Long loggedInUserId = 100L;

    StudyPost studyPost = StudyPost.builder()
        .id(10L)
        .currentParticipants(3)
        .maxParticipants(5)
        .build();

    StudySignup studySignup = StudySignup.builder()
        .id(studySignupId)
        .user(User.builder().id(loggedInUserId).build())
        .status(StudySignupStatus.APPROVED)
        .studyPost(studyPost)
        .build();

    when(studySignupRepository.findById(studySignupId)).thenReturn(Optional.of(studySignup));
    when(authService.getLoginUserId()).thenReturn(loggedInUserId);

    // When
    studySignupService.cancelSignup(studySignupId);

    // Then
    assertEquals(2, studyPost.getCurrentParticipants());
    verify(studySignupRepository).findById(studySignupId);
    verify(authService).getLoginUserId();
    verify(studySignupRepository).delete(studySignup);
    verify(studyPostRepository).save(studyPost);
  }

  @Test
  @DisplayName("신청 취소 실패 - 신청 내역 없음")
  void cancelSignup_Fail_SignupNotFound() {
    // Given
    Long studySignupId = 1L;

    when(studySignupRepository.findById(studySignupId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studySignupService.cancelSignup(studySignupId)
    );

    assertEquals(ErrorCode.SIGNUP_NOT_FOUND, exception.getErrorCode());
    verify(studySignupRepository).findById(studySignupId);
    verifyNoInteractions(authService, studyPostRepository);
  }
}
