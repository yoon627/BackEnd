package com.devonoff.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.dto.UserUpdateRequest;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.LoginType;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private AuthService authService;

  @Mock
  private PhotoService photoService;

  @Mock
  private UserRepository userRepository;

  @Test
  @DisplayName("유저 정보 상세 조회 - 성공")
  void testGetUserDetail_Success() {
    // given
    Long userId = 1L;
    User user = User.builder()
        .id(1L)
        .nickname("testNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .profileImage("testProfileImageUrl")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

    // when
    UserDto userDetail = userService.getUserDetail(userId);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));

    assertThat(userDetail.getId()).isEqualTo(user.getId());
    assertThat(userDetail.getNickname()).isEqualTo(user.getNickname());
    assertThat(userDetail.getEmail()).isEqualTo(user.getEmail());
    assertThat(userDetail.getProfileImageUrl()).isEqualTo(user.getProfileImage());
  }

  @Test
  @DisplayName("유저 정보 상세 조회 - 실패 (로그인한 유저와 불일치)")
  void testGetUserDetail_Fail_UserUnMatched() {
    // given
    Long userId = 2L;

    given(authService.getLoginUserId()).willReturn(1L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.getUserDetail(userId));

    // then
    verify(authService, times(1)).getLoginUserId();

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_USER);
    assertThat(customException.getErrorMessage()).isEqualTo("로그인 된 사용자와 일치하지 않습니다.");
  }

  @Test
  @DisplayName("유저 정보 상세 조회 - 실패 (존재하지 않는 유저)")
  void testGetUserDetail_Fail_UserNotFound() {
    // given
    Long userId = 1L;

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.getUserDetail(userId));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(customException.getErrorMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("유저 정보 수정 - 성공")
  void testUpdateUserInfo_Success() {
    // given
    Long userId = 1L;

    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("updateNickname")
        .build();

    User user = User.builder()
        .id(1L)
        .nickname("testNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .profileImage("testProfileImageUrl")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    User updatedUser = User.builder()
        .id(1L)
        .nickname("updateNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .profileImage("testProfileImageUrl")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    given(userRepository.existsByNickname(eq(userUpdateRequest.getNickname()))).willReturn(false);
    given(userRepository.save(eq(user))).willReturn(updatedUser);

    // when
    UserDto updateUserInfo = userService.updateUserInfo(userId, userUpdateRequest);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));
    verify(userRepository, times(1))
        .existsByNickname(eq(userUpdateRequest.getNickname()));
    verify(userRepository, times(1)).save(any(User.class));

    assertThat(updateUserInfo.getNickname()).isEqualTo(userUpdateRequest.getNickname());
  }

  @Test
  @DisplayName("유저 정보 수정 - 실패 (로그인한 유저와 불일치)")
  void testUpdateUserInfo_Fail_UserUnMatched() {
    // given
    Long userId = 2L;

    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("updateNickname")
        .build();

    given(authService.getLoginUserId()).willReturn(1L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.updateUserInfo(userId, userUpdateRequest));

    // then
    verify(authService, times(1)).getLoginUserId();

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_USER);
    assertThat(customException.getErrorMessage()).isEqualTo("로그인 된 사용자와 일치하지 않습니다.");
  }

  @Test
  @DisplayName("유저 정보 수정 - 실패 (존재하지 않는 유저)")
  void testUpdateUserInfo_Fail_UserNotFound() {
    // given
    Long userId = 1L;

    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("updateNickname")
        .build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.updateUserInfo(userId, userUpdateRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(customException.getErrorMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("유저 정보 수정 - 실패 (닉네임 중복)")
  void testUpdateUserInfo_Fail_NicknameDuplicated() {
    // given
    Long userId = 1L;

    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("duplicatedNickname")
        .build();

    User user = User.builder()
        .id(1L)
        .nickname("testNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .profileImage("testProfileImageUrl")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    given(userRepository.existsByNickname(eq(userUpdateRequest.getNickname()))).willReturn(true);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.updateUserInfo(userId, userUpdateRequest));
    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));
    verify(userRepository, times(1))
        .existsByNickname(eq(userUpdateRequest.getNickname()));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.NICKNAME_ALREADY_REGISTERED);
    assertThat(customException.getErrorMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
  }

  @Test
  @DisplayName("프로필 이미지 수정 - 성공")
  void testUpdateProfileImage_Success() {
    // given
    Long userId = 1L;
    MultipartFile profileImage = mock(MultipartFile.class);

    User user = User.builder()
        .id(1L)
        .nickname("testNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .profileImage("testProfileImageUrl")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    User updatedUser = User.builder()
        .id(1L)
        .nickname("updateNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .profileImage("newProfileImageUrl")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    given(photoService.save(eq(profileImage))).willReturn("newProfileImageUrl");
    given(userRepository.save(eq(user))).willReturn(updatedUser);

    // when
    UserDto responseUserDto = userService.updateProfileImage(userId, profileImage);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));
    verify(photoService, times(1)).save(eq(profileImage));
    verify(userRepository, times(1)).save(eq(user));

    assertThat(responseUserDto.getProfileImageUrl()).isEqualTo("newProfileImageUrl");
  }

  @Test
  @DisplayName("프로필 이미지 수정 - 실패 (로그인한 유저와 불일치)")
  void testUpdateProfileImage_Fail_UserUnMatched() {
    // given
    Long userId = 2L;
    MultipartFile profileImage = mock(MultipartFile.class);

    given(authService.getLoginUserId()).willReturn(1L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.updateProfileImage(userId, profileImage));

    // then
    verify(authService, times(1)).getLoginUserId();

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_USER);
    assertThat(customException.getErrorMessage()).isEqualTo("로그인 된 사용자와 일치하지 않습니다.");
  }

  @Test
  @DisplayName("프로필 이미지 수정 - 실패 (존재하지 않는 유저)")
  void testUpdateProfileImage_Fail_UserNotFound() {
    // given
    Long userId = 1L;
    MultipartFile profileImage = mock(MultipartFile.class);

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.updateProfileImage(userId, profileImage));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(customException.getErrorMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("프로필 이미지 수정 - 실패 (이미지 업로드 실패)")
  void testUpdateProfileImage_Fail_UploadFailed() {
    // given
    Long userId = 1L;
    MultipartFile profileImage = mock(MultipartFile.class);

    User user = User.builder()
        .id(1L)
        .nickname("testNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .profileImage("testProfileImageUrl")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    given(photoService.save(eq(profileImage)))
        .willThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

    // when
    ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,
        () -> userService.updateProfileImage(userId, profileImage));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));
    verify(photoService, times(1)).save(eq(profileImage));

    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  @DisplayName("프로필 이미지 삭제 - 성공")
  void testDeleteProfileImage_Success() {
    // given
    Long userId = 1L;
    User user = User.builder()
        .id(1L)
        .nickname("testNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .profileImage("testProfileImageUrl")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();
    String deleteProfileImageUrl = user.getProfileImage();

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    willDoNothing().given(photoService).delete(eq(deleteProfileImageUrl));

    // when
    userService.deleteProfileImage(userId);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));
    verify(photoService, times(1)).delete(eq(deleteProfileImageUrl));
  }

  @Test
  @DisplayName("프로필 이미지 삭제 - 실패 (로그인한 유저와 불일치)")
  void testDeleteProfileImage_Fail_UserUnMatched() {
    // given
    Long userId = 2L;

    given(authService.getLoginUserId()).willReturn(1L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.deleteProfileImage(userId));

    // then
    verify(authService, times(1)).getLoginUserId();

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_USER);
    assertThat(customException.getErrorMessage()).isEqualTo("로그인 된 사용자와 일치하지 않습니다.");
  }

  @Test
  @DisplayName("프로필 이미지 삭제 - 실패 (존재하지 않는 유저)")
  void testDeleteProfileImage_Fail_UserNotFound() {
    // given
    Long userId = 1L;

    given(authService.getLoginUserId()).willReturn(1L);
    given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> userService.deleteProfileImage(userId));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(userId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(customException.getErrorMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
  }

}