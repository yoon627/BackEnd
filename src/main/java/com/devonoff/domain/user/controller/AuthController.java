package com.devonoff.domain.user.controller;

import com.devonoff.common.dto.ResponseDto;
import com.devonoff.domain.user.dto.auth.CertificationRequest;
import com.devonoff.domain.user.dto.auth.EmailRequest;
import com.devonoff.domain.user.dto.auth.NickNameCheckRequest;
import com.devonoff.domain.user.dto.auth.ReissueTokenRequest;
import com.devonoff.domain.user.dto.auth.ReissueTokenResponse;
import com.devonoff.domain.user.dto.auth.SignInRequest;
import com.devonoff.domain.user.dto.auth.SignInResponse;
import com.devonoff.domain.user.dto.auth.SignUpRequest;
import com.devonoff.domain.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  /**
   * 닉네임 중복 확인
   *
   * @param nickNameCheckRequest
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/check-nickname")
  public ResponseEntity<ResponseDto> checkNicName(
      @RequestBody @Valid NickNameCheckRequest nickNameCheckRequest
  ) {
    return ResponseEntity.ok(authService.nickNameCheck(nickNameCheckRequest));
  }

  /**
   * 이메일 중복 확인
   *
   * @param emailRequest
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/check-email")
  public ResponseEntity<ResponseDto> checkEmail(
      @RequestBody @Valid EmailRequest emailRequest
  ) {
    return ResponseEntity.ok(authService.emailCheck(emailRequest));
  }

  /**
   * 인증번호 이메일 전송
   *
   * @param emailSendRequest
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/email-send")
  public ResponseEntity<ResponseDto> sendEmail(
      @RequestBody @Valid EmailRequest emailSendRequest
  ) {
    return ResponseEntity.ok(authService.emailSend(emailSendRequest));
  }

  /**
   * 이메일 인증번호 확인
   *
   * @param certificationRequest
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/email-certification")
  public ResponseEntity<ResponseDto> certificationEmail(
      @RequestBody @Valid CertificationRequest certificationRequest
  ) {
    return ResponseEntity.ok(authService.certificationEmail(certificationRequest));
  }

  /**
   * 회원가입
   *
   * @param signUpRequest
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/sign-up")
  public ResponseEntity<ResponseDto> signUp(
      @RequestBody @Valid SignUpRequest signUpRequest
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(signUpRequest));
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
   * 로그아웃
   *
   * @param userDetails
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/sign-out")
  public ResponseEntity<ResponseDto> signOut(
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.ok(authService.signOut(userDetails));
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
   * @param userDetails
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/withdrawal")
  public ResponseEntity<ResponseDto> withdrawalUser(
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.ok(authService.withdrawalUser(userDetails));
  }

}
