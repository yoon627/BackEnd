package com.devonoff.domain.user.controller;

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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final SocialAuthService socialAuthService;

  /**
   * 닉네임 중복 확인
   *
   * @param nickNameCheckRequest
   * @return ResponseEntity<Void>
   */
  @PostMapping("/check-nickname")
  public ResponseEntity<Void> checkNicName(
      @RequestBody @Valid NickNameCheckRequest nickNameCheckRequest
  ) {
    authService.nicknameCheck(nickNameCheckRequest);
    return ResponseEntity.ok().build();
  }

  /**
   * 이메일 중복 확인
   *
   * @param emailRequest
   * @return ResponseEntity<Void>
   */
  @PostMapping("/check-email")
  public ResponseEntity<Void> checkEmail(
      @RequestBody @Valid EmailRequest emailRequest
  ) {
    authService.emailCheck(emailRequest);
    return ResponseEntity.ok().build();
  }

  /**
   * 인증번호 이메일 전송
   *
   * @param emailSendRequest
   * @return ResponseEntity<Void>
   */
  @PostMapping("/email-send")
  public ResponseEntity<Void> sendEmail(
      @RequestBody @Valid EmailRequest emailSendRequest
  ) {
    authService.emailSend(emailSendRequest);
    return ResponseEntity.ok().build();
  }

  /**
   * 이메일 인증번호 확인
   *
   * @param certificationRequest
   * @return ResponseEntity<Void>
   */
  @PostMapping("/email-certification")
  public ResponseEntity<Void> certificationEmail(
      @RequestBody @Valid CertificationRequest certificationRequest
  ) {
    authService.certificationEmail(certificationRequest);
    return ResponseEntity.ok().build();
  }

  /**
   * 회원가입
   *
   * @param signUpRequest
   * @return ResponseEntity<Void>
   */
  @PostMapping("/sign-up")
  public ResponseEntity<Void> signUp(
      @RequestBody @Valid SignUpRequest signUpRequest
  ) {
    authService.signUp(signUpRequest);
    return ResponseEntity.ok().build();
  }

  /**
   * 이메일로 로그인
   *
   * @param signInRequest
   * @return ResponseEntity<SignInResponse>
   */
  @PostMapping("/sign-in")
  public ResponseEntity<SignInResponse> signIn(
      @RequestBody @Valid SignInRequest signInRequest
  ) {
    return ResponseEntity.ok(authService.signIn(signInRequest));
  }

  /**
   * 소셜 로그인
   *
   * @param socialAuthRequest
   * @return ResponseEntity<SignInResponse>
   */
  @PostMapping("/sign-in/{provider}")
  public ResponseEntity<SignInResponse> signInSocial(
      @PathVariable String provider,
      @RequestBody @Valid SocialAuthRequest socialAuthRequest
  ) {
    return ResponseEntity.ok(socialAuthService.socialSignIn(provider, socialAuthRequest.getCode()));
  }

  /**
   * 로그아웃
   *
   * @return ResponseEntity<Void>
   */
  @PostMapping("/sign-out")
  public ResponseEntity<Void> signOut() {
    authService.signOut();
    return ResponseEntity.ok().build();
  }

  /**
   * 엑세스 토큰 재발행
   *
   * @param reissueTokenRequest
   * @return ResponseEntity<ReissueTokenResponse>
   */
  @PostMapping("/token-reissue")
  public ResponseEntity<ReissueTokenResponse> reissueToken(
      @RequestBody @Valid ReissueTokenRequest reissueTokenRequest
  ) {
    return ResponseEntity.ok(authService.reissueToken(reissueTokenRequest));
  }

  /**
   * 회원 탈퇴
   *
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/withdrawal")
  public ResponseEntity<Void> withdrawalUser() {
    authService.withdrawalUser();
    return ResponseEntity.ok().build();
  }

}
