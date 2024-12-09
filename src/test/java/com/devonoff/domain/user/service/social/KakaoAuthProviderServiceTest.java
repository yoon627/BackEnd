package com.devonoff.domain.user.service.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.Map;
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
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class KakaoAuthProviderServiceTest {

  @InjectMocks
  private KakaoAuthProviderService kakaoAuthProviderService;

  @Mock
  private RestTemplate restTemplate;

  @Test
  @DisplayName("카카오 API 통신 - 성공 (GetAccessToken)")
  void testKakaoAuthProviderService_Success_GetAccessToken() {
    // given
    String mockCode = "mock-auth-code";
    String mockAccessToken = "mock-access-token";
    String mockTokenUrl = "https://kauth.kakao.com/oauth/token";
    Map<String, String> kakaoTokenResponse = Map.of(
        "access_token", mockAccessToken
    );

    given(restTemplate.exchange(
        eq(mockTokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));

    // when
    String kakaoAccessToken = kakaoAuthProviderService.getAccessToken(mockCode);

    // then
    verify(restTemplate, times(1))
        .exchange(eq(mockTokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));

    assertThat(kakaoAccessToken).isEqualTo(mockAccessToken);
  }

  @Test
  @DisplayName("카카오 API 통신 - 실패 (GetAccessToken)")
  void testKakaoAuthProviderService_Fail_GetAccessToken() {
    // given
    String mockCode = "invalidCode";
    String mockTokenUrl = "https://kauth.kakao.com/oauth/token";

    given(restTemplate.exchange(
        eq(mockTokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class))
    ).willThrow(new RuntimeException("Kakao API Error"));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> kakaoAuthProviderService.getAccessToken(mockCode));

    // then
    verify(restTemplate, times(1))
        .exchange(eq(mockTokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    assertThat(customException.getErrorMessage()).isEqualTo("Kakao AccessToken 요청에 실패했습니다.");
  }

  @Test
  @DisplayName("카카오 API 통신 - 성공 (GetUserInfo)")
  void testKakaoSignIn_Success_UserInfoGetFailed() {
    // given
    String mockAccessToken = "mock-access-token";
    String mockUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
    Map<String, Object> kakaoUserResponse = Map.of(
        "id", "12345",
        "kakao_account", Map.of("email", "test@kakao.com")
    );

    given(restTemplate.exchange(
        eq(mockUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(kakaoUserResponse, HttpStatus.OK));

    // when
    Map<String, Object> kakaoUserInfo = kakaoAuthProviderService.getUserInfo(mockAccessToken);

    // then
    verify(restTemplate, times(1))
        .exchange(eq(mockUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    assertThat(kakaoUserInfo.get("id")).isEqualTo("12345");
    assertThat(kakaoUserInfo.get("email")).isEqualTo("test@kakao.com");
  }

  @Test
  @DisplayName("카카오 API 통신 - 실패 (GetUserInfo)")
  void testKakaoSignIn_Fail_UserInfoGetFailed() {
    // given
    String mockAccessToken = "mock-access-token";
    String mockUserInfoUrl = "https://kapi.kakao.com/v2/user/me";

    given(restTemplate.exchange(
        eq(mockUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
    )).willThrow(new RuntimeException("Kakao UserInfo API Error"));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> kakaoAuthProviderService.getUserInfo(mockAccessToken));

    // then
    verify(restTemplate, times(1))
        .exchange(eq(mockUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    assertThat(customException.getErrorMessage()).isEqualTo("Kakao 유저정보 요청에 실패했습니다.");
  }

}