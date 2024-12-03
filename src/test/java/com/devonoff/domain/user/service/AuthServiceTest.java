package com.devonoff.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devonoff.domain.redis.repository.AuthRedisRepository;
import com.devonoff.domain.user.dto.auth.CertificationRequest;
import com.devonoff.domain.user.dto.auth.EmailRequest;
import com.devonoff.domain.user.dto.auth.NickNameCheckRequest;
import com.devonoff.domain.user.dto.auth.SignUpRequest;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.LoginType;
import com.devonoff.util.EmailProvider;
import com.devonoff.util.JwtProvider;
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
        NickNameCheckRequest.builder().nickName(nickname).build();

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
        NickNameCheckRequest.builder().nickName(nickname).build();

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
        .nickName(nickname)
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
        .nickName(nickname)
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
        .nickName(nickname)
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
        .nickName(nickname)
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

}