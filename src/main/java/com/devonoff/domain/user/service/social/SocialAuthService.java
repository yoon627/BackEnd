package com.devonoff.domain.user.service.social;

import com.devonoff.domain.user.dto.auth.SignInResponse;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.util.JwtProvider;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

  private final List<SocialAuthProviderService> providers;
  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  public SignInResponse socialSignIn(String providerName, String code) {
    SocialAuthProviderService provider = getProvider(providerName);
    String accessToken = provider.getAccessToken(code);
    return processUser(provider, accessToken);
  }

  private SocialAuthProviderService getProvider(String providerName) {
    return providers.stream()
        .filter(provider -> provider.getLoginType().name().equalsIgnoreCase(providerName))
        .findFirst()
        .orElseThrow(
            () -> new CustomException(ErrorCode.BAD_REQUEST, "지원하지 않는 소셜 로그인 제공자입니다.")
        );
  }

  private SignInResponse processUser(SocialAuthProviderService provider, String accessToken) {
    Map<String, Object> userInfo = provider.getUserInfo(accessToken);

    String nickname = provider.getLoginType().name().toLowerCase() + "_" + userInfo.get("id");
    String email = (String) userInfo.get("email");

    User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(
        User.builder()
            .nickname(nickname)
            .email(email)
            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
            .loginType(provider.getLoginType())
            .isActive(true)
            .build()
    ));

    return SignInResponse.builder()
        .accessToken(jwtProvider.createAccessToken(user.getId()))
        .refreshToken(jwtProvider.createRefreshToken(user.getId()))
        .build();
  }

}
