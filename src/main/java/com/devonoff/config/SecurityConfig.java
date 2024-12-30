package com.devonoff.config;

import com.devonoff.util.CustomCorsFilter;
import com.devonoff.util.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final CustomCorsFilter customCorsFilter;

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
            .configurationSource(corsConfigurationSource())
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
                "/api/auth/sign-in/**",
                "/api/auth/token-reissue",
                "/oauth2/**",
                "/signaling/**",
                "/healthcheck",
                "/oauth2/**",
                "/ws/**"
            )
            .permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/study-posts",
                "/api/study-posts/**",
                "/api/study-posts/search",
                "/api/study-posts/search-by-id",
                "/api/info-posts",
                "/api/info-posts/**",
                "/api/qna-posts",
                "/api/qna-posts/**",
                "/api/total-study-time/ranking"
            )
            .permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(customCorsFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();

  }

  /**
   * Cors 정책 설정
   *
   * @return CorsConfigurationSource
   */
  @Bean
  protected CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration corsConfigurationV1 = new CorsConfiguration();
    corsConfigurationV1.addAllowedOrigin("*"); // 명확한 Origin 명시
//    corsConfigurationV1.addAllowedOriginPattern("https://devonoffdoo.vercel.app/**"); // 명확한 Origin 명시
//    corsConfigurationV1.setAllowCredentials(true);
    corsConfigurationV1.addAllowedMethod("*");
    corsConfigurationV1.addAllowedHeader("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfigurationV1);
    source.registerCorsConfiguration("/api/**", corsConfigurationV1);
    source.registerCorsConfiguration("/signaling", corsConfigurationV1);
    source.registerCorsConfiguration("/ws/**", corsConfigurationV1);

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