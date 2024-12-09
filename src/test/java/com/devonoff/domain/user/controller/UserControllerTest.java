package com.devonoff.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.dto.UserUpdateRequest;
import com.devonoff.domain.user.service.UserService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.util.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserService userService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() throws Exception {
    doNothing().when(jwtAuthenticationFilter)
        .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class),
            any(FilterChain.class));
  }

  @Test
  @DisplayName("유저 정보 조회 - 성공")
  void testGetUserDetail_Success() throws Exception {
    // given
    UserDto userDto = UserDto.builder()
        .id(1L)
        .nickname("testNickname")
        .email("test@email.com")
        .profileImageUrl("testProfileImageUrl")
        .build();

    given(userService.getUserDetail(eq(1L))).willReturn(userDto);

    // when, then
    mockMvc.perform(get("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userDto.getId()))
        .andExpect(jsonPath("$.nickname").value(userDto.getNickname()))
        .andExpect(jsonPath("$.email").value(userDto.getEmail()))
        .andExpect(jsonPath("$.profileImageUrl").value(userDto.getProfileImageUrl()));
  }

  @Test
  @DisplayName("유저 정보 조회 - 실패 (로그인한 유저와 불일치)")
  void testGetUserDetail_Fail_UserUnMatched() throws Exception {
    // given
    given(userService.getUserDetail(eq(1L)))
        .willThrow(new CustomException(ErrorCode.UNAUTHORIZED_USER));

    // when, then
    mockMvc.perform(get("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("유저 정보 조회 - 실패 (존재하지 않는 유저)")
  void testGetUserDetail_Fail_UserNotFound() throws Exception {
    // given
    given(userService.getUserDetail(eq(1L)))
        .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

    // when, then
    mockMvc.perform(get("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("유저 정보 수정 - 성공")
  void testUpdateUserInfo_Success() throws Exception {
    // given
    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("newNickname")
        .build();

    UserDto userDto = UserDto.builder()
        .id(1L)
        .nickname("newNickname")
        .email("test@email.com")
        .profileImageUrl("testProfileImageUrl")
        .build();

    given(userService.updateUserInfo(eq(1L), any(UserUpdateRequest.class)))
        .willReturn(userDto);

    // when, then
    mockMvc.perform(put("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userDto.getId()))
        .andExpect(jsonPath("$.nickname").value(userDto.getNickname()))
        .andExpect(jsonPath("$.email").value(userDto.getEmail()))
        .andExpect(jsonPath("$.profileImageUrl").value(userDto.getProfileImageUrl()));
  }

  @Test
  @DisplayName("유저 정보 수정 - 실패 (로그인한 유저와 불일치)")
  void testUpdateUserInfo_Fail_UserUnMatched() throws Exception {
    // given
    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("newNickname")
        .build();

    given(userService.updateUserInfo(eq(1L), any(UserUpdateRequest.class)))
        .willThrow(new CustomException(ErrorCode.UNAUTHORIZED_USER));

    // when, then
    mockMvc.perform(put("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("유저 정보 수정 - 실패 (존재하지 않는 유저)")
  void testUpdateUserInfo_Fail_UserNotFound() throws Exception {
    // given
    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("newNickname")
        .build();

    given(userService.updateUserInfo(eq(1L), any(UserUpdateRequest.class)))
        .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

    // when, then
    mockMvc.perform(put("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("유저 정보 수정 - 실패 (닉네임 중복)")
  void testUpdateUserInfo_Fail_NicknameDuplicated() throws Exception {
    // given
    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("newNickname")
        .build();

    given(userService.updateUserInfo(eq(1L), any(UserUpdateRequest.class)))
        .willThrow(new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED));

    // when, then
    mockMvc.perform(put("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("프로필 이미지 수정 - 성공")
  void testUpdateProfileImage_Success() throws Exception {
    // given
    UserDto userDto = UserDto.builder()
        .id(1L)
        .nickname("newNickname")
        .email("test@email.com")
        .profileImageUrl("testProfileImageUrl")
        .build();

    given(userService.updateProfileImage(eq(1L), any(MultipartFile.class)))
        .willReturn(userDto);

    // when, then
    mockMvc.perform(post("/api/users/1/profile-image")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userDto.getId()))
        .andExpect(jsonPath("$.nickname").value(userDto.getNickname()))
        .andExpect(jsonPath("$.email").value(userDto.getEmail()))
        .andExpect(jsonPath("$.profileImageUrl").value(userDto.getProfileImageUrl()));
  }

  @Test
  @DisplayName("프로필 이미지 수정 - 실패 (로그인한 유저와 불일치)")
  void testUpdateProfileImage_Fail_UserUnMatched() throws Exception {
    // given
    given(userService.updateProfileImage(eq(1L), any(MultipartFile.class)))
        .willThrow(new CustomException(ErrorCode.UNAUTHORIZED_USER));

    // when, then
    mockMvc.perform(post("/api/users/1/profile-image")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("프로필 이미지 수정 - 실패 (존재하지 않는 유저)")
  void testUpdateProfileImage_Fail_UserNotFound() throws Exception {
    // given
    given(userService.updateProfileImage(eq(1L), any(MultipartFile.class)))
        .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

    // when, then
    mockMvc.perform(post("/api/users/1/profile-image")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("프로필 이미지 수정 - 실패 (이미지 업로드 실패)")
  void testUpdateProfileImage_Fail_UploadImageFailed() throws Exception {
    // given
    given(userService.updateProfileImage(eq(1L), any(MultipartFile.class)))
        .willThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

    // when, then
    mockMvc.perform(post("/api/users/1/profile-image")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("프로필 이미지 삭제 - 성공")
  void testDeleteProfileImage_Success() throws Exception {
    // given
    willDoNothing().given(userService).deleteProfileImage(eq(1L));

    // when, then
    mockMvc.perform(delete("/api/users/1/profile-image")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("프로필 이미지 삭제 - 실패 (로그인한 유저와 불일치)")
  void testDeleteProfileImage_Fail_UserUnMatched() throws Exception {
    // given
    doThrow(new CustomException(ErrorCode.UNAUTHORIZED_USER))
        .when(userService).deleteProfileImage(eq(1L));

    // when, then
    mockMvc.perform(delete("/api/users/1/profile-image")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("프로필 이미지 삭제 - 실패 (로그인한 유저와 불일치)")
  void testDeleteProfileImage_Fail_UserNotFound() throws Exception {
    // given
    doThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
        .when(userService).deleteProfileImage(eq(1L));

    // when, then
    mockMvc.perform(delete("/api/users/1/profile-image")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

}