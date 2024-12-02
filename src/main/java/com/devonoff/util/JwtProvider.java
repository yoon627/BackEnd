package com.devonoff.util;


import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour
  private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 3; // 3 Days

  private final UserRepository userRepository;

  @Value("${spring.jwt.secret}")
  private String secretKey;

  public String createAccessToken(Long userId) {
    return createToken(userId, ACCESS_TOKEN_EXPIRE_TIME);
  }

  public String createRefreshToken(Long userId) {
    return createToken(userId, REFRESH_TOKEN_EXPIRE_TIME);
  }

  /**
   * 사용자 ID 정보를 포함한 JWT 토큰 생성
   *
   * @param userId
   * @return String
   */
  public String createToken(Long userId, long expireTime) {
    Map<String, Object> claims = new HashMap<>();

    return Jwts.builder()
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .setClaims(claims)
        .setSubject(String.valueOf(userId))
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expireTime))
        .compact();
  }

  /**
   * SecurityContextHolder 에 등록하기위한 토큰에 있는 사용자 객체 및 권한 정보로 Authentication 객체 생성
   *
   * @param jwt
   * @return Authentication
   */
  public Authentication getAuthentication(String jwt) {
    User user = userRepository.findById(getUserId(jwt))
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
  }

  /**
   * 토큰에 있는 사용자 ID 추출
   *
   * @param token
   * @return String
   */
  public Long getUserId(String token) {
    return Long.valueOf(parseClaims(token).getSubject());
  }


  /**
   * 파싱한 토큰이 유효한지 검증
   *
   * @param token
   * @return boolean
   */
  public boolean validateToken(String token) {
    if (!StringUtils.hasText(token)) {
      return false;
    }

    Claims claims = parseClaims(token);
    return !claims.getExpiration().before(new Date());
  }

  /**
   * 토큰 서명, 유효기간 검증 및 파싱 (String -> Claims)
   *
   * @param token
   * @return Claims
   */
  private Claims parseClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }
}
