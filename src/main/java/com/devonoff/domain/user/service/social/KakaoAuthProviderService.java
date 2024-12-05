package com.devonoff.domain.user.service.social;

import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.LoginType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoAuthProviderService implements SocialAuthProviderService {

  private final RestTemplate restTemplate;

  @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
  private String kakaoClientId;
  @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
  private String kakaoClientSecret;
  @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  @Override
  public String getAccessToken(String code) {
    String tokenUrl = "https://kauth.kakao.com/oauth/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", kakaoClientId);
    body.add("client_secret", kakaoClientSecret);
    body.add("redirect_uri", kakaoRedirectUri);
    body.add("code", code);

    try {
      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

      return response.getBody().get("access_token").toString();
    } catch(Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Kakao AccessToken 요청에 실패했습니다.");
    }
  }

  @Override
  public Map<String, Object> getUserInfo(String accessToken) {
    String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    try {
      HttpEntity<Void> request = new HttpEntity<>(headers);
      ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

      Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");

      return Map.of(
          "id", response.getBody().get("id"),
          "email", kakaoAccount.get("email")
      );
    } catch(Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Kakao 유저정보 요청에 실패했습니다.");
    }
  }

  @Override
  public LoginType getLoginType() {
    return LoginType.KAKAO;
  }
}
