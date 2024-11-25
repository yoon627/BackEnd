package com.devonoff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


  // 비밀번호 암호화용 Bean 등록
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // CSRF 비활성화
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/").permitAll() // 인증 없이 접근 가능
            .anyRequest().authenticated() // 나머지는 인증 필요
        )
        .httpBasic(Customizer.withDefaults()); // 기본 HTTP Basic 인증 활성화

    return http.build();
  }

}