package com.devonoff.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.user.dto.auth.CertificationRequest;
import com.devonoff.domain.user.dto.auth.EmailRequest;
import com.devonoff.domain.user.dto.auth.NickNameCheckRequest;
import com.devonoff.domain.user.dto.auth.ReissueTokenRequest;
import com.devonoff.domain.user.dto.auth.ReissueTokenResponse;
import com.devonoff.domain.user.dto.auth.SignInRequest;
import com.devonoff.domain.user.dto.auth.SignInResponse;
import com.devonoff.domain.user.dto.auth.SignUpRequest;
import com.devonoff.domain.user.dto.auth.SocialAuthRequest;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.domain.user.service.social.SocialAuthService;
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

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  @MockBean
  private SocialAuthService socialAuthService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() throws Exception {
    doNothing().when(jwtAuthenticationFilter)
        .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class),
            any(FilterChain.class));
  }

  @Test
  @DisplayName("닉네임 중복 확인 - 성공")
  void testCheckNickname_Success() throws Exception {
    // given
    NickNameCheckRequest nickNameCheckRequest = NickNameCheckRequest.builder()
        .nickname("testNickname")
        .build();

    willDoNothing().given(authService).nicknameCheck(nickNameCheckRequest);

    // when, then
    mockMvc.perform(post("/api/auth/check-nickname")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nickNameCheckRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("닉네임 중복 확인 - 실패 (유효성 검증 실패)")
  void testCheckNickname_Fail_ValidationFail() throws Exception {
    // given
    NickNameCheckRequest nickNameCheckRequest = NickNameCheckRequest.builder().build();

    // when, then
    mockMvc.perform(post("/api/auth/check-nickname")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nickNameCheckRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("이메일 중복 확인 - 성공")
  void testCheckEmail_Success() throws Exception {
    // given
    EmailRequest emailRequest = EmailRequest.builder()
        .email("test@email.com")
        .build();
    willDoNothing().given(authService).emailCheck(emailRequest);

    // when, then
    mockMvc.perform(post("/api/auth/check-email")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(emailRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("이메일 중복 확인 - 실패 (유효성 검증 실패)")
  void testCheckEmail_Fail_ValidationFail() throws Exception {
    // given
    EmailRequest emailRequest = EmailRequest.builder().build();

    // when, then
    mockMvc.perform(post("/api/auth/check-email")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(emailRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("인증번호 이메일 전송 - 성공")
  void testSendEmail_Success() throws Exception {
    // given
    EmailRequest emailRequest = EmailRequest.builder()
        .email("test@email.com")
        .build();

    willDoNothing().given(authService).emailSend(emailRequest);

    // when, then
    mockMvc.perform(post("/api/auth/email-send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(emailRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("인증번호 이메일 전송 - 실패 (유효성 검증 실패)")
  void testSendEmail_Fail_ValidationFail() throws Exception {
    // given
    EmailRequest emailRequest = EmailRequest.builder().build();

    // when, then
    mockMvc.perform(post("/api/auth/email-send")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(emailRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("인증번호 확인 - 성공")
  void testCertificationEmail_Success() throws Exception {
    // given
    CertificationRequest certificationRequest = CertificationRequest.builder()
        .email("test@email.com")
        .certificationNumber("1234")
        .build();

    willDoNothing().given(authService).certificationEmail(certificationRequest);

    // when, then
    mockMvc.perform(post("/api/auth/email-certification")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(certificationRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("인증번호 확인 - 실패 (유효성 검증 실패)")
  void testCertificationEmail_Fail_ValidationFail() throws Exception {
    // given
    CertificationRequest certificationRequest = CertificationRequest.builder().build();

    // when, then
    mockMvc.perform(post("/api/auth/email-certification")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(certificationRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원가입 - 성공")
  void testSignUp_Success() throws Exception {
    // given
    SignUpRequest signUpRequest = SignUpRequest.builder()
        .nickname("testNickname")
        .email("test@email.com")
        .password("testPassword")
        .build();

    willDoNothing().given(authService).signUp(signUpRequest);

    // when, then
    mockMvc.perform(post("/api/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("회원가입 - 실패 (유효성 검증 실패)")
  void testSignUp_Fail_ValidationFail() throws Exception {
    // given
    SignUpRequest signUpRequest = SignUpRequest.builder()
        .email("test@email.com")
        .password("testPassword")
        .build();

    // when, then
    mockMvc.perform(post("/api/auth/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("로그인 - 성공")
  void testSignIn_Success() throws Exception {
    // given
    SignInRequest signInRequest = SignInRequest.builder()
        .email("test@email.com")
        .password("testPassword")
        .build();

    SignInResponse signInResponse = SignInResponse.builder()
        .accessToken("AccessToken")
        .refreshToken("RefreshToken")
        .build();

    given(authService.signIn(any(SignInRequest.class))).willReturn(signInResponse);

    // when, then
    mockMvc.perform(post("/api/auth/sign-in")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signInRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("AccessToken"))
        .andExpect(jsonPath("$.refreshToken").value("RefreshToken"));
  }

  @Test
  @DisplayName("로그인 - 실패 (유효성 검증 실패)")
  void testSignIn_Fail_ValidationFail() throws Exception {
    // given
    SignInRequest signInRequest = SignInRequest.builder()
        .password("testPassword")
        .build();

    // when, then
    mockMvc.perform(post("/api/auth/sign-in")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signInRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("소셜 로그인 (카카오) - 성공")
  void testSocialSignInKakao_Success() throws Exception {
    // given
    SocialAuthRequest socialAuthRequest = SocialAuthRequest.builder()
        .code("Authentication-Code")
        .build();

    SignInResponse signInResponse = SignInResponse.builder()
        .accessToken("AccessToken")
        .refreshToken("RefreshToken")
        .build();

    given(socialAuthService.socialSignIn(anyString(), anyString())).willReturn(signInResponse);

    // when, then
    mockMvc.perform(post("/api/auth/sign-in/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(socialAuthRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("AccessToken"))
        .andExpect(jsonPath("$.refreshToken").value("RefreshToken"));
  }

  @Test
  @DisplayName("소셜 로그인 - 실패 (유효성 검증 실패)")
  void testSocialSignIn_Fail_ValidationFailed() throws Exception {
    // given
    SocialAuthRequest socialAuthRequest = SocialAuthRequest.builder().build();

    // when, then
    mockMvc.perform(post("/api/auth/sign-in/kakao")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(socialAuthRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("로그아웃 - 성공")
  void testSignOut_Success() throws Exception {
    // given
    willDoNothing().given(authService).signOut();

    // when, then
    mockMvc.perform(post("/api/auth/sign-out"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Access Token 재발급 - 성공")
  void testReissueToken_Success() throws Exception {
    // given
    ReissueTokenRequest reissueTokenRequest = ReissueTokenRequest.builder()
        .email("test@email.com")
        .refreshToken("RefreshToken")
        .build();

    ReissueTokenResponse reissueTokenResponse = ReissueTokenResponse.builder()
        .accessToken("newAccessToken")
        .build();

    given(authService.reissueToken(any(ReissueTokenRequest.class))).willReturn(
        reissueTokenResponse);

    // when, then
    mockMvc.perform(post("/api/auth/token-reissue")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reissueTokenRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("newAccessToken"));
  }

  @Test
  @DisplayName("Access Token 재발급 - 실패 (유효성 검증 실패)")
  void testReissueToken_Fail_ValidationFail() throws Exception {
    // given
    ReissueTokenRequest reissueTokenRequest = ReissueTokenRequest.builder()
        .refreshToken("RefreshToken")
        .build();

    // when, then
    mockMvc.perform(post("/api/auth/token-reissue")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reissueTokenRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원 탈퇴 - 성공")
  void testWithdrawalUser_Success() throws Exception {
    // given
    willDoNothing().given(authService).withdrawalUser();

    // when, then
    mockMvc.perform(post("/api/auth/withdrawal"))
        .andExpect(status().isOk());
  }
}