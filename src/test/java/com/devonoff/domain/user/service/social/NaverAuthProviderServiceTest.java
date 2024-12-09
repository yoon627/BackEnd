package com.devonoff.domain.user.service.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
class NaverAuthProviderServiceTest {

  @InjectMocks
  private NaverAuthProviderService naverAuthProviderService;

  @Mock
  private RestTemplate restTemplate;

  @Test
  @DisplayName("네이버 API 통신 - 성공 (GetAccessToken)")
  void testNaverAuthProviderService_Success_GetAccessToken() {
    // given
    String mockCode = "mock-auth-code";
    String mockAccessToken = "mock-access-token";
    String mockTokenUrl = "https://nid.naver.com/oauth2.0/token";
    Map<String, String> naverTokenResponse = Map.of(
        "access_token", mockAccessToken
    );

    given(restTemplate.exchange(
        eq(mockTokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(naverTokenResponse, HttpStatus.OK));

    // when
    String naverAccessToken = naverAuthProviderService.getAccessToken(mockCode);

    // then
    verify(restTemplate, times(1))
        .exchange(eq(mockTokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));

    assertThat(naverAccessToken).isEqualTo(mockAccessToken);
  }

  @Test
  @DisplayName("네이버 API 통신 - 실패 (GetAccessToken)")
  void testNaverAuthProviderService_Fail_GetAccessToken() {
    // given
    String mockCode = "invalidCode";
    String mockTokenUrl = "https://nid.naver.com/oauth2.0/token";

    given(restTemplate.exchange(
        eq(mockTokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class))
    ).willThrow(new RuntimeException("Naver API Error"));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> naverAuthProviderService.getAccessToken(mockCode));

    // then
    verify(restTemplate, times(1))
        .exchange(eq(mockTokenUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    assertThat(customException.getErrorMessage()).isEqualTo("Naver AccessToken 요청에 실패했습니다.");
  }

  @Test
  @DisplayName("네이버 API 통신 - 성공 (GetUserInfo)")
  void testNaverSignIn_Success_UserInfoGetFailed() {
    // given
    String mockAccessToken = "mock-access-token";
    String mockUserInfoUrl = "https://openapi.naver.com/v1/nid/me";
    Map<String, Object> naverUserResponse = Map.of(
        "response", Map.of("id", "uCiyxwaQVpvKj_CBm", "email", "test@naver.com")
    );

    given(restTemplate.exchange(
        eq(mockUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
    )).willReturn(new ResponseEntity<>(naverUserResponse, HttpStatus.OK));

    // when
    Map<String, Object> naverUserInfo = naverAuthProviderService.getUserInfo(mockAccessToken);

    // then
    verify(restTemplate, times(1))
        .exchange(eq(mockUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    assertThat(naverUserInfo.get("id")).isEqualTo("uCiyxwaQVpvKj_CBm");
    assertThat(naverUserInfo.get("email")).isEqualTo("test@naver.com");
  }

  @Test
  @DisplayName("네이버 API 통신 - 실패 (GetUserInfo)")
  void testNaverSignIn_Fail_UserInfoGetFailed() {
    // given
    String mockAccessToken = "mock-access-token";
    String mockUserInfoUrl = "https://openapi.naver.com/v1/nid/me";

    given(restTemplate.exchange(
        eq(mockUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
    )).willThrow(new RuntimeException("Naver UserInfo API Error"));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> naverAuthProviderService.getUserInfo(mockAccessToken));

    // then
    verify(restTemplate, times(1))
        .exchange(eq(mockUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    assertThat(customException.getErrorMessage()).isEqualTo("Naver 유저정보 요청에 실패했습니다.");
  }

}