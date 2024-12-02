package com.devonoff.domain.user.service;

import com.devonoff.common.dto.ResponseDto;
import com.devonoff.domain.redis.service.RedisService;
import com.devonoff.domain.user.dto.auth.CertificationRequest;
import com.devonoff.domain.user.dto.auth.EmailRequest;
import com.devonoff.domain.user.dto.auth.NickNameCheckRequest;
import com.devonoff.domain.user.dto.auth.ReissueTokenRequest;
import com.devonoff.domain.user.dto.auth.ReissueTokenResponse;
import com.devonoff.domain.user.dto.auth.SignInRequest;
import com.devonoff.domain.user.dto.auth.SignInResponse;
import com.devonoff.domain.user.dto.auth.SignUpRequest;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.LoginType;
import com.devonoff.util.CertificationNumber;
import com.devonoff.util.EmailProvider;
import com.devonoff.util.JwtProvider;
import jakarta.transaction.Transactional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;

  private final RedisService redisService;

  private final EmailProvider emailProvider;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  /**
   * 사용자 NickName 중복 체크
   *
   * @param nickNameCheckRequest
   * @return ResponseDto
   */
  public ResponseDto nickNameCheck(NickNameCheckRequest nickNameCheckRequest) {
    checkExistsNickName(nickNameCheckRequest.getNickName());
    return ResponseDto.getResponseBody("사용 가능한 닉네임 입니다.");
  }

  /**
   * 사용자 Email 중복 체크
   *
   * @param emailRequest
   * @return ResponseDto
   */
  public ResponseDto emailCheck(EmailRequest emailRequest) {
    checkExistsEmail(emailRequest.getEmail());
    return ResponseDto.getResponseBody("사용 가능한 이메일 입니다.");
  }

  /**
   * 이메일 인증번호 전송
   *
   * @param emailSendRequest
   * @return ResponseDto
   */
  public ResponseDto emailSend(EmailRequest emailSendRequest) {
    String email = emailSendRequest.getEmail();

    checkExistsEmail(email);

    String certificationNumber = CertificationNumber.getCertificationNumber();

    boolean isSucceed =
        emailProvider.sendCertificationMail(email, certificationNumber);
    if (!isSucceed) {
      throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
    }

    // Redis 에 email 을 키값으로 CertificationNumber 를 저장. (유효시간 3분으로 설정)
    redisService.saveDataWithTTL(email, certificationNumber, 3, TimeUnit.MINUTES);
    return ResponseDto.getResponseBody("이메일 전송에 성공했습니다.");
  }

  /**
   * 인증번호 확인
   *
   * @param certificationRequest
   * @return ResponseDto
   */
  public ResponseDto certificationEmail(CertificationRequest certificationRequest) {
    boolean isVerified = redisService.verifyCertificationNumber(
        certificationRequest.getEmail(), certificationRequest.getCertificationNumber()
    );

    if (!isVerified) {
      throw new CustomException(ErrorCode.EMAIL_VERIFICATION_FAILED);
    }

    return ResponseDto.getResponseBody("이메일 인증에 성공했습니다.");
  }

  /**
   * 회원가입
   *
   * @param signUpRequest
   * @return ResponseDto
   */
  @Transactional
  public ResponseDto signUp(SignUpRequest signUpRequest) {
    String nickName = signUpRequest.getNickName();
    String email = signUpRequest.getEmail();

    checkExistsNickName(nickName);
    checkExistsEmail(email);

    boolean isCheckVerified = redisService.checkVerified(email + ":verified");
    if (!isCheckVerified) {
      throw new CustomException(ErrorCode.EMAIL_VERIFICATION_UNCOMPLETED);
    }

    String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());

    userRepository.save(
        User.builder()
            .nickName(nickName)
            .email(email)
            .password(encodedPassword)
            .isActive(true)
            .loginType(LoginType.GENERAL)
            .build()
    );

    return ResponseDto.getResponseBody("회원가입이 완료되었습니다.");
  }

  /**
   * 로그인
   *
   * @param request
   * @return SignInResponse
   */
  public SignInResponse signIn(SignInRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!user.getIsActive()) {
      throw new CustomException(ErrorCode.ACCOUNT_PENDING_DELETION);
    }

    boolean isMatched = passwordEncoder.matches(request.getPassword(), user.getPassword());
    if (!isMatched) {
      throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken = jwtProvider.createAccessToken(user.getId());
    String refreshToken = jwtProvider.createRefreshToken(user.getId());

    // 리프레쉬 토큰을 3일 동안만 레디스에 저장
    redisService.saveDataWithTTL(user.getEmail() + "-refreshToken", refreshToken, 3, TimeUnit.DAYS);

    return SignInResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  /**
   * 로그아웃
   *
   * @param userId
   * @return ResponseDto
   */
  public ResponseDto signOut(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    redisService.deleteToken(user.getEmail() + "-refreshToken");

    return ResponseDto.getResponseBody("로그아웃 되었습니다.");
  }

  /**
   * refresh 토큰 확인 후 accessToken 재 발행
   *
   * @param reissueTokenRequest
   * @return ReissueTokenResponse
   */
  public ReissueTokenResponse reissueToken(ReissueTokenRequest reissueTokenRequest) {
    String refreshTokenKey = reissueTokenRequest.getEmail() + "-refreshToken";
    redisService.checkRefreshToken(refreshTokenKey, reissueTokenRequest.getRefreshToken());

    User user = userRepository.findByEmail(reissueTokenRequest.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String accessToken = jwtProvider.createAccessToken(user.getId());

    return ReissueTokenResponse.builder()
        .accessToken(accessToken)
        .build();
  }

  /**
   * 회원 탈퇴
   *
   * @param userId
   * @return ResponseDto
   */
  @Transactional
  public ResponseDto withdrawalUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    user.setIsActive(false);

    return ResponseDto.getResponseBody("회원 탈퇴 처리 되었습니다.");
  }

  /**
   * 이미 존재하는 nickName 인지 확인
   *
   * @param nickName
   */
  public void checkExistsNickName(String nickName) {
    boolean existsByNickName = userRepository.existsByNickName(nickName);
    if (existsByNickName) {
      throw new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED);
    }
  }

  /**
   * 이미 존재하는 email 인지 확인
   *
   * @param email
   */
  public void checkExistsEmail(String email) {
    boolean existsByEmail = userRepository.existsByEmail(email);
    if (existsByEmail) {
      throw new CustomException(ErrorCode.EMAIL_ALREADY_REGISTERED);
    }
  }

  public Long getLoginUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    return Long.parseLong(userDetails.getUsername());
  }

}
