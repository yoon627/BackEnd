package com.devonoff.config;

import com.devonoff.util.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configurable
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
//  private final DefaultOAuth2UserService oAuth2UserService;
//  private final OAuth2SuccessHandler oAuth2SuccessHandler;

  /**
   * Security Filter Chain 설정
   *
   * @param httpSecurity
   * @return SecurityFilterChain
   * @throws Exception
   */
  @Bean
  protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {

    httpSecurity
        .cors(cors -> cors
            .configurationSource(corsConfigurationSorce())
        )
        .csrf(CsrfConfigurer::disable)
        .httpBasic(HttpBasicConfigurer::disable)
        .sessionManagement(sessionManagement -> sessionManagement
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(request -> request
            .requestMatchers(
                "/",
                "/api/auth/check-nickname",
                "/api/auth/check-email",
                "/api/auth/email-send",
                "/api/auth/email-certification",
                "/api/auth/sign-up",
                "/api/auth/sign-in",
                "/api/auth/token-reissue",
                "/oauth2/**"
            )
            .permitAll()
            .anyRequest().authenticated()
        )
//        .oauth2Login(oauth2 -> oauth2
//            .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/v1/auth/oauth2"))
//            .redirectionEndpoint(endpoint -> endpoint.baseUri("/oauth2/callback/*"))
//            .userInfoEndpoint(endpoint -> endpoint.userService(oAuth2UserService))
//            .successHandler(oAuth2SuccessHandler)
//        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();

  }

  /**
   * Cors 정책 설정
   *
   * @return CorsConfigurationSource
   */
  @Bean
  protected CorsConfigurationSource corsConfigurationSorce() {

    CorsConfiguration corsConfigurationV1 = new CorsConfiguration();
    corsConfigurationV1.addAllowedOrigin("*");
    corsConfigurationV1.addAllowedMethod("*");
    corsConfigurationV1.addAllowedHeader("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/v1/**", corsConfigurationV1);

    return source;
  }

  /**
   * 사용자 비밀번호 암호화를 위한 인코더 설정
   *
   * @return PasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}