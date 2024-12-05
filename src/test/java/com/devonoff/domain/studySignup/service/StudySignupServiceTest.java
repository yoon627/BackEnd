package com.devonoff.domain.studySignup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
