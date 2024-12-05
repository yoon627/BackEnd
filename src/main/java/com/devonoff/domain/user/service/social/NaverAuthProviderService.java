package com.devonoff.domain.user.service.social;

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
public class NaverAuthProviderService implements SocialAuthProviderService {

  private final RestTemplate restTemplate;

  @Value("${spring.security.oauth2.client.registration.naver.client-id}")
  private String naverClientId;
  @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
  private String naverClientSecret;
  @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
  private String naverRedirectUri;

  @Override
  public String getAccessToken(String code) {
    String tokenUrl = "https://nid.naver.com/oauth2.0/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", naverClientId);
    body.add("client_secret", naverClientSecret);
    body.add("redirect_uri", naverRedirectUri);
    body.add("code", code);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
    ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

    return response.getBody().get("access_token").toString();
  }

  @Override
  public Map<String, Object> getUserInfo(String accessToken) {
    String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

    return (Map<String, Object>) response.getBody().get("response");
  }

  @Override
  public LoginType getLoginType() {
    return LoginType.NAVER;
  }
}
