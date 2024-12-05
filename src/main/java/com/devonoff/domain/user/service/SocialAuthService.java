package com.devonoff.domain.user.service;

import com.devonoff.domain.user.dto.auth.SignInResponse;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.LoginType;
import com.devonoff.util.JwtProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

  private final RestTemplate restTemplate;

  private final UserRepository userRepository;

  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
  private String kakaoClientId;
  @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
  private String kakaoClientSecret;
  @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  @Value("${spring.security.oauth2.client.registration.naver.client-id}")
  private String naverClientId;
  @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
  private String naverClientSecret;
  @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
  private String naverRedirectUri;

  /**
   * Kakao 로그인
   *
   * @param code
   * @return SignInResponse
   */
  public SignInResponse kakaoSignIn(String code) {
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
      ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request,
          Map.class);
      return getKakaoUserInfo(response.getBody().get("access_token").toString());
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "KAKAO 로그인에 실패했습니다.");
    }
  }

  /**
   * Kakao 유저정보 추출후 User 테이블에 저장 및 Access Token, Refresh Token 발급
   *
   * @param kakaoAccessToken
   * @return SignInResponse
   */
  private SignInResponse getKakaoUserInfo(String kakaoAccessToken) {
    String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(kakaoAccessToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request,
        Map.class);

    Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody()
        .get("kakao_account");

    String kakaoNickname = "kakao_" + response.getBody().get("id").toString();
    String kakaoEmail = kakaoAccount.get("email").toString();

    User kakaoUser = userRepository.findByEmail(kakaoEmail).orElse(
        User.builder()
            .nickname(kakaoNickname)
            .email(kakaoEmail)
            .password(passwordEncoder.encode("kakaoPassword"))
            .isActive(true)
            .loginType(LoginType.KAKAO)
            .build()
    );

    User savedUser = userRepository.save(kakaoUser);

    String accessToken = jwtProvider.createAccessToken(savedUser.getId());
    String refreshToken = jwtProvider.createRefreshToken(savedUser.getId());

    return SignInResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  /**
   * Naver 로그인
   *
   * @param code
   * @return SignInResponse
   */
  public SignInResponse naverSignIn(String code) {
    String tokenUrl = "https://nid.naver.com/oauth2.0/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", naverClientId);
    body.add("client_secret", naverClientSecret);
    body.add("redirect_uri", naverRedirectUri);
    body.add("code", code);

    try {
      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request,
          Map.class);

      return getNaverUserInfo(response.getBody().get("access_token").toString());
    } catch(Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Naver 로그인에 실패했습니다.");
    }

  }

  /**
   * Naver 유저정보 추출후 User 테이블에 저장 및 Access Token, Refresh Token 발급
   *
   * @param naverAccessToken
   * @return SignInResponse
   */
  private SignInResponse getNaverUserInfo(String naverAccessToken) {
    String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(naverAccessToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request,
        Map.class);

    Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("response");

    String naverNickname = "naver_" + responseData.get("id").toString();
    String naverEmail = responseData.get("email").toString();

    User naverUser = userRepository.findByEmail(naverEmail).orElse(
        User.builder()
            .nickname(naverNickname)
            .email(naverEmail)
            .password(passwordEncoder.encode("naverPassword"))
            .isActive(true)
            .loginType(LoginType.NAVER)
            .build()
    );

    User savedUser = userRepository.save(naverUser);

    String accessToken = jwtProvider.createAccessToken(savedUser.getId());
    String refreshToken = jwtProvider.createRefreshToken(savedUser.getId());

    return SignInResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

}
