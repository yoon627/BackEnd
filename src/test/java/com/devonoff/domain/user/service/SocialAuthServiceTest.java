package com.devonoff.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devonoff.domain.user.dto.auth.SignInResponse;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.social.SocialAuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.util.JwtProvider;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

  @InjectMocks
  private SocialAuthService socialAuthService;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("카카오 로그인 - 성공")
  void testKakaoSignIn_Success() {
    // given
    String mockCode = "mock-auth-code";
    String tokenUrl = "https://kauth.kakao.com/oauth/token";
    String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
    Map<String, String> kakaoTokenResponse = Map.of(
        "access_token", "mock-kakao-access-token"
    );
    Map<String, Object> kakaoUserResponse = Map.of(
        "id", "123456789",
        "kakao_account", Map.of("email", "test@kakao.com")
    );

    given(restTemplate.exchange(
        eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));

    given(restTemplate.exchange(
        eq(userInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(kakaoUserResponse, HttpStatus.OK));

    given(userRepository.findByEmail(eq("test@kakao.com"))).willReturn(Optional.empty());
    given(passwordEncoder.encode(eq("kakaoPassword"))).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willReturn(
        User.builder()
            .id(1L)
            .nickname("kakao_123456789")
            .email("test@kakao.com")
            .password("encodedPassword")
            .build()
    );

    given(jwtProvider.createAccessToken(eq(1L))).willReturn("AccessToken");
    given(jwtProvider.createRefreshToken(eq(1L))).willReturn("RefreshToken");

    // when
    SignInResponse signInResponse = socialAuthService.kakaoSignIn(mockCode);

    // then
    verify(restTemplate, times(1))
        .exchange(eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    verify(restTemplate, times(1))
        .exchange(eq(userInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    verify(userRepository, times(1))
        .findByEmail(eq("test@kakao.com"));
    verify(userRepository, times(1))
        .save(any(User.class));

    assertThat(signInResponse.getAccessToken()).isEqualTo("AccessToken");
    assertThat(signInResponse.getRefreshToken()).isEqualTo("RefreshToken");
  }

  @Test
  @DisplayName("카카오 로그인 - 실패 (카카오 AccesToken 획득 실패)")
  void testKakaoSignIn_Fail_AccessTokenGetFailed() {
    // given
    String mockCode = "invalidCode";
    String tokenUrl = "https://kauth.kakao.com/oauth/token";

    given(restTemplate.exchange(
        eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class))
    ).willThrow(new RuntimeException("Kakao API Error"));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> socialAuthService.kakaoSignIn(mockCode));

    // then
    verify(restTemplate, times(1))
        .exchange(eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    assertThat(customException.getErrorMessage()).isEqualTo("KAKAO 로그인에 실패했습니다.");
  }

  @Test
  @DisplayName("카카오 로그인 - 실패 (카카오 유저정보 획득 실패)")
  void testKakaoSignIn_Fail_UserInfoGetFailed() {
    // given
    String mockCode = "mock-auth-code";
    Map<String, String> kakaoTokenResponse = Map.of(
        "access_token", "invalidAccessToken"
    );
    String tokenUrl = "https://kauth.kakao.com/oauth/token";
    String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

    given(restTemplate.exchange(
        eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));

    given(restTemplate.exchange(
        eq(userInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
    )).willThrow(new RuntimeException("Kakao UserInfo API Error"));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> socialAuthService.kakaoSignIn(mockCode));

    // then
    verify(restTemplate, times(1))
        .exchange(eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    verify(restTemplate, times(1))
        .exchange(eq(userInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    assertThat(customException.getErrorMessage()).isEqualTo("KAKAO 로그인에 실패했습니다.");
  }

  @Test
  @DisplayName("네이버 로그인 - 성공")
  void testNaverSignIn_Success() {
    // given
    String mockCode = "mock-auth-code";
    String tokenUrl = "https://nid.naver.com/oauth2.0/token";
    String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
    Map<String, String> naverTokenResponse = Map.of(
        "access_token", "mock-naver-access-token"
    );
    Map<String, Object> naverUserResponse = Map.of(
        "response", Map.of("id", "uCiyxwaQVpvKj_CBm", "email", "test@naver.com")
    );

    given(restTemplate.exchange(
        eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(naverTokenResponse, HttpStatus.OK));

    given(restTemplate.exchange(
        eq(userInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(naverUserResponse, HttpStatus.OK));

    given(userRepository.findByEmail(eq("test@naver.com"))).willReturn(Optional.empty());
    given(passwordEncoder.encode(eq("naverPassword"))).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willReturn(
        User.builder()
            .id(1L)
            .nickname("naver_uCiyxwaQVpvKj_CBm")
            .email("test@naver.com")
            .password("encodedPassword")
            .build()
    );

    given(jwtProvider.createAccessToken(eq(1L))).willReturn("AccessToken");
    given(jwtProvider.createRefreshToken(eq(1L))).willReturn("RefreshToken");

    // when
    SignInResponse signInResponse = socialAuthService.naverSignIn(mockCode);

    // then
    verify(restTemplate, times(1))
        .exchange(eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    verify(restTemplate, times(1))
        .exchange(eq(userInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    verify(userRepository, times(1))
        .findByEmail(eq("test@naver.com"));
    verify(userRepository, times(1))
        .save(any(User.class));

    assertThat(signInResponse.getAccessToken()).isEqualTo("AccessToken");
    assertThat(signInResponse.getRefreshToken()).isEqualTo("RefreshToken");
  }

  @Test
  @DisplayName("네이버 로그인 - 실패 (네이버 AccesToken 획득 실패)")
  void testNaverSignIn_Fail_AccessTokenGetFail() {
    // given
    String mockCode = "invalidCode";
    String tokenUrl = "https://nid.naver.com/oauth2.0/token";

    given(restTemplate.exchange(
        eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
    )).willThrow(new RuntimeException("Naver Api Error"));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> socialAuthService.naverSignIn(mockCode));

    // then
    verify(restTemplate, times(1))
        .exchange(eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    assertThat(customException.getErrorMessage()).isEqualTo("Naver 로그인에 실패했습니다.");
  }

  @Test
  @DisplayName("네이버 로그인 - 실패 (네이버 유저정보 획득 실패)")
  void testNaverSignIn_Fail_UserInfoGetFail() {
    // given
    String mockCode = "mock-auth-code";
    String tokenUrl = "https://nid.naver.com/oauth2.0/token";
    String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
    Map<String, String> naverTokenResponse = Map.of(
        "access_token", "invalidAccessToken"
    );

    given(restTemplate.exchange(
        eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(naverTokenResponse, HttpStatus.OK));

    given(restTemplate.exchange(
        eq(userInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
    )).willThrow(new RuntimeException("Naver UserInfo API Error"));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> socialAuthService.naverSignIn(mockCode));

    // then
    verify(restTemplate, times(1))
        .exchange(eq(tokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    verify(restTemplate, times(1))
        .exchange(eq(userInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    assertThat(customException.getErrorMessage()).isEqualTo("Naver 로그인에 실패했습니다.");
  }

}