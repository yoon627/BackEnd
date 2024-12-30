package com.devonoff.domain.user.service;

import com.devonoff.domain.redis.repository.AuthRedisRepository;
import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.student.service.StudentService;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studySignup.repository.StudySignupRepository;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.dto.auth.CertificationRequest;
import com.devonoff.domain.user.dto.auth.EmailRequest;
import com.devonoff.domain.user.dto.auth.NickNameCheckRequest;
import com.devonoff.domain.user.dto.auth.PasswordChangeRequest;
import com.devonoff.domain.user.dto.auth.ReissueTokenRequest;
import com.devonoff.domain.user.dto.auth.ReissueTokenResponse;
import com.devonoff.domain.user.dto.auth.SignInRequest;
import com.devonoff.domain.user.dto.auth.SignInResponse;
import com.devonoff.domain.user.dto.auth.SignUpRequest;
import com.devonoff.domain.user.dto.auth.WithdrawalRequest;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.LoginType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.util.CertificationNumber;
import com.devonoff.util.EmailProvider;
import com.devonoff.util.JwtProvider;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final AuthRedisRepository authRedisRepository;
  private final EmailProvider emailProvider;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;
  private final StudentRepository studentRepository;
  private final StudentService studentService;
  private final StudySignupRepository studySignupRepository;
  private final StudyPostRepository studyPostRepository;
  @Value("${cloud.aws.s3.default-profile-image-url}")
  private String defaultProfileImageUrl;

  /**
   * 사용자 Nickname 중복 체크
   *
   * @param nickNameCheckRequest
   */
  public void nicknameCheck(NickNameCheckRequest nickNameCheckRequest) {
    checkExistsNickName(nickNameCheckRequest.getNickname());
  }

  /**
   * 이메일 인증번호 전송
   *
   * @param emailSendRequest
   */
  public void emailSend(EmailRequest emailSendRequest) {
    String email = emailSendRequest.getEmail();

    checkExistsEmail(email);

    String certificationNumber = CertificationNumber.getCertificationNumber();

    boolean isSucceed = emailProvider.sendCertificationMail(email, certificationNumber);
    if (!isSucceed) {
      throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
    }

    // Redis 에 email 을 키값으로 CertificationNumber 를 저장. (유효시간 3분으로 설정)
    authRedisRepository.setData(email, certificationNumber, 3, TimeUnit.MINUTES);
  }

  /**
   * 인증번호 확인
   *
   * @param certificationRequest
   */
  public void certificationEmail(CertificationRequest certificationRequest) {
    String email = certificationRequest.getEmail();
    String savedCertificationNumber = authRedisRepository.getData(email);

    if (savedCertificationNumber == null) {
      throw new CustomException(ErrorCode.EXPIRED_EMAIL_CODE);
    }

    if (!savedCertificationNumber.equals(certificationRequest.getCertificationNumber())) {
      throw new CustomException(ErrorCode.INVALID_EMAIL_CODE);
    }

    // Redis 에 UserId 를 키값으로 인증 완료 여부를 저장. (유효시간 1시간으로 설정)
    authRedisRepository.setData(email + ":certificated", "true", 1, TimeUnit.HOURS);
    // 기존에 Redis 에 UserId 를 키값으로 가지는 인증번호 데이터는 유효시간에 관계없이 삭제
    authRedisRepository.deleteData(email);
  }

  /**
   * 회원가입
   *
   * @param signUpRequest
   * @return ResponseDto
   */
  public void signUp(SignUpRequest signUpRequest) {
    String nickName = signUpRequest.getNickname();
    String email = signUpRequest.getEmail();

    checkExistsNickName(nickName);
    checkExistsEmail(email);

    String isCheckVerified = authRedisRepository.getData(email + ":certificated");
    if (isCheckVerified == null) {
      throw new CustomException(ErrorCode.EMAIL_CERTIFICATION_UNCOMPLETED);
    }

    String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());

    userRepository.save(
        User.builder()
            .nickname(nickName)
            .email(email)
            .password(encodedPassword)
            .isActive(true)
            .loginType(LoginType.GENERAL)
            .profileImage(defaultProfileImageUrl)
            .build()
    );
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
    authRedisRepository.setData(
        user.getEmail() + "-refreshToken", refreshToken, 3, TimeUnit.DAYS
    );

    return SignInResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  /**
   * 로그아웃
   */
  public void signOut() {
    Long loginUserId = getLoginUserId();
    User user = userRepository.findById(loginUserId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    authRedisRepository.deleteData(user.getEmail() + "-refreshToken");
  }

  /**
   * 비밀번호 변경
   *
   * @param userId
   * @param passwordChangeRequest
   * @return UserDto
   */
  public UserDto changePassword(Long userId, PasswordChangeRequest passwordChangeRequest) {
    Long loginUserId = getLoginUserId();
    if (!loginUserId.equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    String currentPassword = passwordChangeRequest.getCurrentPassword();
    String newPassword = passwordChangeRequest.getNewPassword();
    if (currentPassword.equals(newPassword)) {
      throw new CustomException(ErrorCode.SAME_PASSWORD);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_PASSWORD);
    }

    user.setPassword(passwordEncoder.encode(newPassword));

    return UserDto.fromEntity(userRepository.save(user));
  }

  /**
   * refresh 토큰 확인 후 accessToken 재 발행
   *
   * @param reissueTokenRequest
   * @return ReissueTokenResponse
   */
  public ReissueTokenResponse reissueToken(ReissueTokenRequest reissueTokenRequest) {
    Long userId = jwtProvider.getUserId(reissueTokenRequest.getRefreshToken());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String refreshTokenKey = user.getEmail() + "-refreshToken";
    String refreshTokenData = authRedisRepository.getData(refreshTokenKey);

    if (refreshTokenData == null) {
      throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    if (!refreshTokenData.equals(reissueTokenRequest.getRefreshToken())) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    String accessToken = jwtProvider.createAccessToken(user.getId());

    return ReissueTokenResponse.builder()
        .accessToken(accessToken)
        .build();
  }

  /**
   * 회원 탈퇴
   *
   * @param withdrawalRequest
   */
  @Transactional
  public void withdrawalUser(WithdrawalRequest withdrawalRequest) {
    Long loginUserId = getLoginUserId();
    User user = userRepository.findById(loginUserId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (user.getLoginType() == LoginType.GENERAL) {
      if (withdrawalRequest == null) {
        throw new CustomException(ErrorCode.PASSWORD_IS_NULL);
      }

      boolean isMatch = passwordEncoder.matches(withdrawalRequest.getPassword(),
          user.getPassword());
      if (!isMatch) {
        throw new CustomException(ErrorCode.INVALID_PASSWORD);
      }
    }

    // 탈퇴한 사용자가 속한 모든 스터디에서 스터디 참가자 제거
    List<Student> userStudents = studentRepository.findByUser(user);
    for (Student student : userStudents) {
      studentService.removeStudent(student.getId());
    }

    // 탈퇴한 회원의 스터디 신청 내역을 삭제
    studySignupRepository.deleteAllByUser(user);

    // 탈퇴한 회원의 스터디 모집 게시글을 모집 취소로 변경
    List<StudyPost> studyPosts = studyPostRepository.findAllByUser(user);
    for (StudyPost studyPost : studyPosts) {
      studyPost.setStatus(StudyPostStatus.CANCELED);
    }

    authRedisRepository.deleteData(user.getEmail() + "-refreshToken");

    // 삭제 하지 않고 탈퇴한유저 처리
    user.setNickname("탈퇴한 회원");
    user.setEmail("deleted@email.com");
    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
    user.setProfileImage(defaultProfileImageUrl);
    user.setIsActive(false);

    userRepository.save(user);
  }

  /**
   * 이미 존재하는 nickName 인지 확인
   *
   * @param nickName
   */
  private void checkExistsNickName(String nickName) {
    if (userRepository.existsByNickname(nickName)) {
      throw new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED);
    }
  }

  /**
   * 이미 존재하는 email 인지 확인
   *
   * @param email
   */
  private void checkExistsEmail(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new CustomException(ErrorCode.EMAIL_ALREADY_REGISTERED);
    }
  }

  /**
   * SecurityContext 에 있는 유저 ID 정보 추출
   *
   * @return Long
   */
  public Long getLoginUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    return Long.parseLong(userDetails.getUsername());
  }

}
