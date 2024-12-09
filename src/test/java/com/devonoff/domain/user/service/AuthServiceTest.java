package com.devonoff.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devonoff.domain.redis.repository.AuthRedisRepository;
import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.student.service.StudentService;
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
import com.devonoff.util.EmailProvider;
import com.devonoff.util.JwtProvider;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthRedisRepository authRedisRepository;

  @Mock
  private StudentRepository studentRepository;

  @Mock
  private StudentService studentService;

  @Mock
  private EmailProvider emailProvider;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Mock
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.setContext(securityContext);
    given(securityContext.getAuthentication()).willReturn(authentication);
    given(authentication.getPrincipal()).willReturn(userDetails);
    given(userDetails.getUsername()).willReturn("1"); // Mocked user ID
  }

  @Test
  @DisplayName("사용자 Nickname 중복 체크 - 성공")
  void testNicknameCheck_Success() {
    // given
    String nickname = "testNickname";

    NickNameCheckRequest nickNameCheckRequest =
        NickNameCheckRequest.builder().nickname(nickname).build();

    given(userRepository.existsByNickname(eq(nickname))).willReturn(false);

    // when
    authService.nicknameCheck(nickNameCheckRequest);

    // then
    verify(userRepository, times(1)).existsByNickname(eq(nickname));
  }

  @Test
  @DisplayName("사용자 Nickname 중복 체크 - 실패(Nickname 중복)")
  void testNicknameCheck_Fail_NicknameDuplicated() {
    // given
    String nickname = "testNickname";

    NickNameCheckRequest nickNameCheckRequest =
        NickNameCheckRequest.builder().nickname(nickname).build();

    given(userRepository.existsByNickname(eq(nickname))).willReturn(true);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.nicknameCheck(nickNameCheckRequest));

    // then
    verify(userRepository, times(1)).existsByNickname(eq(nickname));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.NICKNAME_ALREADY_REGISTERED);
  }

  @Test
  @DisplayName("사용자 Email 중복 체크 - 성공")
  void testEmailCheck_Success() {
    // given
    String email = "test@email.com";

    EmailRequest emailRequest = EmailRequest.builder().email(email).build();

    given(userRepository.existsByEmail(eq(email))).willReturn(false);

    // when
    authService.emailCheck(emailRequest);

    // then
    verify(userRepository, times(1)).existsByEmail(eq(email));
  }

  @Test
  @DisplayName("사용자 Email 중복 체크 - 실패(Email 중복)")
  void testEmailCheck_Fail_EmailDuplicated() {
    // given
    String email = "test@email.com";

    EmailRequest emailRequest = EmailRequest.builder().email(email).build();

    given(userRepository.existsByEmail(eq(email))).willReturn(true);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.emailCheck(emailRequest));

    // then
    verify(userRepository, times(1)).existsByEmail(eq(email));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_REGISTERED);
  }

  @Test
  @DisplayName("이메일 인증번호 전송 - 성공")
  void testEmailSend_Success() {
    // given
    String email = "test@email.com";

    EmailRequest emailRequest = EmailRequest.builder().email(email).build();

    given(userRepository.existsByEmail(eq(email))).willReturn(false);
    given(emailProvider.sendCertificationMail(eq(email), anyString())).willReturn(true);
    willDoNothing().given(authRedisRepository)
        .setData(eq(email), anyString(), eq(3L), eq(TimeUnit.MINUTES));

    // when
    authService.emailSend(emailRequest);

    // then
    verify(userRepository, times(1)).existsByEmail(eq(email));
    verify(emailProvider, times(1))
        .sendCertificationMail(eq(email), anyString());
    verify(authRedisRepository, times(1))
        .setData(eq(email), anyString(), eq(3L), eq(TimeUnit.MINUTES));
  }

  @Test
  @DisplayName("이메일 인증번호 전송 - 실패 (이메일 중복)")
  void testEmailSend_Fail_EmailDuplicated() {
    // given
    String email = "test@email.com";

    EmailRequest emailRequest = EmailRequest.builder().email(email).build();

    given(userRepository.existsByEmail(eq(email))).willReturn(true);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.emailSend(emailRequest));

    // then
    verify(userRepository, times(1)).existsByEmail(eq(email));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_REGISTERED);
  }

  @Test
  @DisplayName("이메일 인증번호 전송 - 실패 (메일 전송 실패)")
  void testEmailSend_Fail_EmailSendFail() {
    // given
    String email = "test@email.com";

    EmailRequest emailRequest = EmailRequest.builder().email(email).build();

    given(userRepository.existsByEmail(eq(email))).willReturn(false);
    given(emailProvider.sendCertificationMail(eq(email), anyString())).willReturn(false);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.emailSend(emailRequest));

    // then
    verify(userRepository, times(1)).existsByEmail(eq(email));
    verify(emailProvider, times(1))
        .sendCertificationMail(eq(email), anyString());

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_SEND_FAILED);
  }

  @Test
  @DisplayName("인증번호 확인 - 성공")
  void testCertificationEmail_Success() {
    // given
    String email = "test@email.com";
    String certificationNumber = "1234";

    CertificationRequest certificationRequest = CertificationRequest.builder()
        .email(email)
        .certificationNumber(certificationNumber)
        .build();

    given(authRedisRepository.getData(eq(email))).willReturn(certificationNumber);
    willDoNothing().given(authRedisRepository)
        .setData(eq(email + ":certificated"), eq("true"), eq(1L), eq(TimeUnit.HOURS));
    willDoNothing().given(authRedisRepository).deleteData(eq(email));

    // when
    authService.certificationEmail(certificationRequest);

    // then
    verify(authRedisRepository, times(1)).getData(eq(email));
    verify(authRedisRepository, times(1))
        .setData(eq(email + ":certificated"), eq("true"), eq(1L), eq(TimeUnit.HOURS));
    verify(authRedisRepository, times(1)).deleteData(eq(email));
  }

  @Test
  @DisplayName("인증번호 확인 - 실패 (인증번호 만료)")
  void testCertificationEmail_Fail_CertificationNumberExpired() {
    // given
    String email = "test@email.com";
    String certificationNumber = "1234";

    CertificationRequest certificationRequest = CertificationRequest.builder()
        .email(email)
        .certificationNumber(certificationNumber)
        .build();

    given(authRedisRepository.getData(eq(email))).willReturn(null);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.certificationEmail(certificationRequest));

    // then
    verify(authRedisRepository, times(1)).getData(eq(email));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_EMAIL_CODE);
  }

  @Test
  @DisplayName("인증번호 확인 - 실패 (인증번호 불일치)")
  void testCertificationEmail_Fail_CertificationNumberUnMatched() {
    // given
    String email = "test@email.com";
    String certificationNumber = "1234";

    CertificationRequest certificationRequest = CertificationRequest.builder()
        .email(email)
        .certificationNumber(certificationNumber)
        .build();

    given(authRedisRepository.getData(eq(email))).willReturn("2345");

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.certificationEmail(certificationRequest));

    // then
    verify(authRedisRepository, times(1)).getData(eq(email));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_EMAIL_CODE);
  }

  @Test
  @DisplayName("회원가입 - 성공")
  void testSignUp_Success() {
    // given
    String email = "test@email.com";
    String nickname = "testNickname";
    String password = "testPassword";

    SignUpRequest signUpRequest = SignUpRequest.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .build();

    User user = User.builder()
        .nickname(nickname)
        .email(email)
        .password("encodedPassword")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(userRepository.existsByNickname(eq(nickname))).willReturn(false);
    given(userRepository.existsByEmail(eq(email))).willReturn(false);
    given(authRedisRepository.getData(eq(email + ":certificated"))).willReturn("true");
    given(passwordEncoder.encode(eq(password))).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    authService.signUp(signUpRequest);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // then
    verify(userRepository, times(1)).existsByNickname(eq(nickname));
    verify(userRepository, times(1)).existsByEmail(eq(email));
    verify(authRedisRepository, times(1))
        .getData(eq(email + ":certificated"));
    verify(passwordEncoder, times(1)).encode(eq(password));
    verify(userRepository, times(1)).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getNickname()).isEqualTo(user.getNickname());
    assertThat(savedUser.getEmail()).isEqualTo(user.getEmail());
    assertThat(savedUser.getPassword()).isEqualTo(user.getPassword());
    assertThat(savedUser.getIsActive()).isEqualTo(user.getIsActive());
    assertThat(savedUser.getLoginType()).isEqualTo(user.getLoginType());
  }

  @Test
  @DisplayName("회원가입 - 실패 (닉네임 중복)")
  void testSignUp_Fail_NicknameDuplicated() {
    // given
    String email = "test@email.com";
    String nickname = "testNickname";
    String password = "testPassword";

    SignUpRequest signUpRequest = SignUpRequest.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .build();

    given(userRepository.existsByNickname(eq(nickname))).willReturn(true);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.signUp(signUpRequest));

    // then
    verify(userRepository, times(1)).existsByNickname(eq(nickname));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.NICKNAME_ALREADY_REGISTERED);
  }

  @Test
  @DisplayName("회원가입 - 실패 (이메일 중복)")
  void testSignUp_Fail_EmailDuplicated() {
    // given
    String email = "test@email.com";
    String nickname = "testNickname";
    String password = "testPassword";

    SignUpRequest signUpRequest = SignUpRequest.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .build();

    given(userRepository.existsByNickname(eq(nickname))).willReturn(false);
    given(userRepository.existsByEmail(eq(email))).willReturn(true);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.signUp(signUpRequest));

    // then
    verify(userRepository, times(1)).existsByNickname(eq(nickname));
    verify(userRepository, times(1)).existsByEmail(eq(email));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_REGISTERED);
  }

  @Test
  @DisplayName("회원가입 - 실패 (이메일 인증 미완료)")
  void testSignUp_Fail_EmailCertificationUnCompleted() {
    // given
    String email = "test@email.com";
    String nickname = "testNickname";
    String password = "testPassword";

    SignUpRequest signUpRequest = SignUpRequest.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .build();

    given(userRepository.existsByNickname(eq(nickname))).willReturn(false);
    given(userRepository.existsByEmail(eq(email))).willReturn(false);
    given(authRedisRepository.getData(eq(email + ":certificated"))).willReturn(null);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.signUp(signUpRequest));

    // then
    verify(userRepository, times(1)).existsByNickname(eq(nickname));
    verify(userRepository, times(1)).existsByEmail(eq(email));
    verify(authRedisRepository, times(1))
        .getData(eq(email + ":certificated"));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_CERTIFICATION_UNCOMPLETED);
  }

  @Test
  @DisplayName("로그인 - 성공")
  void testLogin_Success() {
    // given
    String email = "test@email.com";
    String password = "testPassword";

    SignInRequest signInRequest = SignInRequest.builder()
        .email(email)
        .password(password)
        .build();

    User user = User.builder()
        .nickname("testNickname")
        .email(email)
        .password("encodedPassword")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(userRepository.findByEmail(eq(email))).willReturn(Optional.of(user));
    given(passwordEncoder.matches(eq(password), eq(user.getPassword()))).willReturn(true);
    given(jwtProvider.createAccessToken(eq(user.getId()))).willReturn("AccessToken");
    given(jwtProvider.createRefreshToken(eq(user.getId()))).willReturn("RefreshToken");

    willDoNothing().given(authRedisRepository)
        .setData(eq(email + "-refreshToken"), eq("RefreshToken"), eq(3L), eq(TimeUnit.DAYS));

    // when
    SignInResponse signInResponse = authService.signIn(signInRequest);

    // then
    verify(userRepository, times(1)).findByEmail(eq(email));
    verify(passwordEncoder, times(1))
        .matches(eq(password), eq(user.getPassword()));
    verify(jwtProvider, times(1)).createAccessToken(eq(user.getId()));
    verify(jwtProvider, times(1)).createRefreshToken(eq(user.getId()));
    verify(authRedisRepository, times(1))
        .setData(eq(email + "-refreshToken"), eq("RefreshToken"), eq(3L), eq(TimeUnit.DAYS));

    assertThat(signInResponse.getAccessToken()).isEqualTo("AccessToken");
    assertThat(signInResponse.getRefreshToken()).isEqualTo("RefreshToken");
  }

  @Test
  @DisplayName("로그인 - 실패 (존재하지 않는 유저)")
  void testLogin_Fail_UserNotFound() {
    // given
    String email = "test@email.com";
    String password = "testPassword";

    SignInRequest signInRequest = SignInRequest.builder()
        .email(email)
        .password(password)
        .build();

    given(userRepository.findByEmail(eq(email))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.signIn(signInRequest));

    // then
    verify(userRepository, times(1)).findByEmail(eq(email));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("로그인 - 실패 (탈퇴한 유저)")
  void testLogin_Fail_WithdrawalUser() {
    // given
    String email = "test@email.com";
    String password = "testPassword";

    SignInRequest signInRequest = SignInRequest.builder()
        .email(email)
        .password(password)
        .build();

    User user = User.builder()
        .nickname("testNickname")
        .email(email)
        .password("encodedPassword")
        .isActive(false)
        .loginType(LoginType.GENERAL)
        .build();

    given(userRepository.findByEmail(eq(email))).willReturn(Optional.of(user));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.signIn(signInRequest));

    // then
    verify(userRepository, times(1)).findByEmail(eq(email));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_PENDING_DELETION);
  }

  @Test
  @DisplayName("로그인 - 실패 (비밀번호 불일치)")
  void testLogin_Fail_PasswordUnMatched() {
    // given
    String email = "test@email.com";
    String password = "testPassword";

    SignInRequest signInRequest = SignInRequest.builder()
        .email(email)
        .password(password)
        .build();

    User user = User.builder()
        .nickname("testNickname")
        .email(email)
        .password("encodedPassword")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(userRepository.findByEmail(eq(email))).willReturn(Optional.of(user));
    given(passwordEncoder.matches(eq(password), eq(user.getPassword()))).willReturn(false);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.signIn(signInRequest));

    // then
    verify(userRepository, times(1)).findByEmail(eq(email));
    verify(passwordEncoder, times(1))
        .matches(eq(password), eq(user.getPassword()));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  @DisplayName("로그아웃 - 성공")
  void testSignOut_Success() {
    // given
    Long userId = 1L;

    User user = User.builder()
        .nickname("testNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    willDoNothing().given(authRedisRepository)
        .deleteData(eq(user.getEmail() + "-refreshToken"));

    // when
    authService.signOut();

    // then
    verify(userRepository, times(1)).findById(eq(userId));
    verify(authRedisRepository, times(1))
        .deleteData(eq(user.getEmail() + "-refreshToken"));
  }

  @Test
  @DisplayName("로그아웃 - 실패 (존재하지 않는 유저)")
  void testSignOut_Fail_UserNotFound() {
    // given
    Long userId = 1L;

    given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.signOut());

    // then
    verify(userRepository, times(1)).findById(eq(userId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("AccessToken 재발급 - 성공")
  void testReissueToken_Success() {
    // given
    String email = "test@email.com";
    String refreshToken = "RefreshToken";

    ReissueTokenRequest reissueTokenRequest = ReissueTokenRequest.builder()
        .email(email)
        .refreshToken(refreshToken)
        .build();

    User user = User.builder()
        .nickname("testNickname")
        .email(email)
        .password("encodedPassword")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(userRepository.findByEmail(eq(email))).willReturn(Optional.of(user));
    given(authRedisRepository.getData(eq(email + "-refreshToken"))).willReturn(refreshToken);
    given(jwtProvider.createAccessToken(eq(user.getId()))).willReturn("AccessToken");

    // when
    ReissueTokenResponse reissueTokenResponse = authService.reissueToken(reissueTokenRequest);

    // then
    verify(userRepository, times(1)).findByEmail(eq(email));
    verify(authRedisRepository, times(1))
        .getData(eq(email + "-refreshToken"));
    verify(jwtProvider, times(1)).createAccessToken(eq(user.getId()));

    assertThat(reissueTokenResponse.getAccessToken()).isEqualTo("AccessToken");
  }

  @Test
  @DisplayName("AccessToken 재발급 - 실패 (존재하지 않는 유저)")
  void testReissueToken_Fail_UserNotFound() {
    // given
    String email = "test@email.com";
    String refreshToken = "RefreshToken";

    ReissueTokenRequest reissueTokenRequest = ReissueTokenRequest.builder()
        .email(email)
        .refreshToken(refreshToken)
        .build();

    given(userRepository.findByEmail(eq(email))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.reissueToken(reissueTokenRequest));

    // then
    verify(userRepository, times(1)).findByEmail(eq(email));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("AccessToken 재발급 - 실패 (RefreshToken 만료)")
  void testReissueToken_Fail_ExpiredRefreshToken() {
    // given
    String email = "test@email.com";
    String refreshToken = "RefreshToken";

    ReissueTokenRequest reissueTokenRequest = ReissueTokenRequest.builder()
        .email(email)
        .refreshToken(refreshToken)
        .build();

    User user = User.builder()
        .nickname("testNickname")
        .email(email)
        .password("encodedPassword")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(userRepository.findByEmail(eq(email))).willReturn(Optional.of(user));
    given(authRedisRepository.getData(eq(email + "-refreshToken"))).willReturn(null);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.reissueToken(reissueTokenRequest));

    // then
    verify(userRepository, times(1)).findByEmail(eq(email));
    verify(authRedisRepository, times(1))
        .getData(eq(email + "-refreshToken"));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
  }

  @Test
  @DisplayName("AccessToken 재발급 - 실패 (유효하지 않은 RefreshToken)")
  void testReissueToken_Fail_InvalidRefreshToken() {
    // given
    String email = "test@email.com";
    String refreshToken = "RefreshToken";

    ReissueTokenRequest reissueTokenRequest = ReissueTokenRequest.builder()
        .email(email)
        .refreshToken(refreshToken)
        .build();

    User user = User.builder()
        .nickname("testNickname")
        .email(email)
        .password("encodedPassword")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    given(userRepository.findByEmail(eq(email))).willReturn(Optional.of(user));
    given(authRedisRepository.getData(eq(email + "-refreshToken")))
        .willReturn("refresh-token");

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.reissueToken(reissueTokenRequest));

    // then
    verify(userRepository, times(1)).findByEmail(eq(email));
    verify(authRedisRepository, times(1))
        .getData(eq(email + "-refreshToken"));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
  }

  @Test
  @DisplayName("회원 탈퇴 - 성공")
  void testWithdrawalUser_Success() {
    // given
    Long userId = 1L;

    User user = User.builder()
        .nickname("testNickname")
        .email("test@email.com")
        .password("encodedPassword")
        .isActive(true)
        .loginType(LoginType.GENERAL)
        .build();

    List<Student> userStudents = List.of(
        Student.builder().id(1L).user(user).build(),
        Student.builder().id(2L).user(user).build()
    );

    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    given(studentRepository.findByUser(eq(user))).willReturn(userStudents);

    willDoNothing().given(studentService).removeStudent(anyLong());
    willDoNothing().given(authRedisRepository)
        .deleteData(eq(user.getEmail() + "-refreshToken"));

    // when
    authService.withdrawalUser();

    // then
    verify(userRepository, times(1)).findById(eq(userId));
    verify(studentRepository, times(1)).findByUser(eq(user));
    verify(studentService, times(2)).removeStudent(anyLong());
    verify(authRedisRepository, times(1))
        .deleteData(eq(user.getEmail() + "-refreshToken"));
    verify(userRepository, times(1)).save(eq(user));

    assertThat(user.getIsActive()).isFalse();
  }

  @Test
  @DisplayName("회원 탈퇴 - 실패 (존재하지 않는 유저)")
  void testWithdrawalUser_Fail_UserNotFound() {
    // given
    Long userId = 1L;

    given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> authService.withdrawalUser());

    // then
    verify(userRepository, times(1)).findById(eq(userId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
  }
}