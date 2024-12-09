package com.devonoff.domain.user.service.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devonoff.domain.user.dto.auth.SignInResponse;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.LoginType;
import com.devonoff.util.JwtProvider;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

  @InjectMocks
  private SocialAuthService socialAuthService;

  @Mock
  private List<SocialAuthProviderService> providers;

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("소셜 로그인 - 성공")
  void testSocialSignIn_Success() {
    // given
    String mockCode = "mock-auth-code";
    String mockAccessToken = "SocialAccessToken";
    User savedUser = User.builder()
        .id(1L)
        .nickname("kakao_12345")
        .email("test@email.com")
        .password("encodedPassword")
        .loginType(LoginType.KAKAO)
        .isActive(true)
        .build();

    SocialAuthProviderService mockProvider = mock(SocialAuthProviderService.class);
    given(providers.stream()).willReturn(Stream.of(mockProvider));
    given(mockProvider.getLoginType()).willReturn(LoginType.KAKAO);
    given(mockProvider.getAccessToken(eq(mockCode))).willReturn(mockAccessToken);
    given(mockProvider.getUserInfo(eq(mockAccessToken))).willReturn(
        Map.of("id", "12345", "email", "test@email.com") // 오타 수정
    );

    given(userRepository.findByEmail(eq("test@email.com"))).willReturn(Optional.empty());
    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willReturn(savedUser);

    given(jwtProvider.createAccessToken(eq(1L))).willReturn("AccessToken");
    given(jwtProvider.createRefreshToken(eq(1L))).willReturn("RefreshToken");

    // when
    SignInResponse signInResponse = socialAuthService.socialSignIn("kakao", mockCode);

    // then
    verify(mockProvider, times(1)).getAccessToken(eq(mockCode));
    verify(mockProvider, times(1)).getUserInfo(eq(mockAccessToken));
    verify(userRepository, times(1)).findByEmail(eq("test@email.com"));
    verify(passwordEncoder, times(1)).encode(anyString());
    verify(userRepository, times(1)).save(any(User.class));
    verify(jwtProvider, times(1)).createAccessToken(eq(1L));
    verify(jwtProvider, times(1)).createRefreshToken(eq(1L));

    assertThat(signInResponse.getAccessToken()).isEqualTo("AccessToken");
    assertThat(signInResponse.getRefreshToken()).isEqualTo("RefreshToken");
  }

  @Test
  @DisplayName("소셜 로그인 - 실패 (지원하지 않는 제공자)")
  void testSocialSignIn_Fail_UnsupportedProvider() {
    // given
    String mockCode = "mock-auth-code";
    given(providers.stream()).willReturn(Stream.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> socialAuthService.socialSignIn("google", mockCode));

    // then
    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
    assertThat(customException.getErrorMessage()).isEqualTo("지원하지 않는 소셜 로그인 제공자입니다.");
  }

}