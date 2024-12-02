package com.devonoff.domain.redis.service;

import com.devonoff.domain.redis.repository.RedisRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

  private final RedisRepository redisRepository;

  /**
   * 유효시간을 포함한 key, value 데이터 저장
   *
   * @param key
   * @param value
   * @param timeout
   * @param unit
   */
  public void saveDataWithTTL(String key, Object value, long timeout, TimeUnit unit) {
    redisRepository.setData(key, value, timeout, unit);
  }

  /**
   * 리프레쉬 토큰이 유효한지 검사
   *
   * @param key
   * @param refreshToken
   */
  public void checkRefreshToken(String key, String refreshToken) {
    Object refreshTokenData = redisRepository.getData(key);

    if (refreshTokenData == null) {
      throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    if (!refreshTokenData.equals(refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
  }

  /**
   * 로그아웃시 Redis 에 저장된 Token 정보 삭제
   *
   * @param key
   */
  public void deleteToken(String key) {
    redisRepository.deleteData(key);
  }

  /**
   * 이메일로 전송받은 인증 번호 확인
   *
   * @param key
   * @param certificationNumber
   * @return boolean
   */
  public boolean verifyCertificationNumber(String key, String certificationNumber) {
    Object storedCertificationNumber = redisRepository.getData(key);

    if (storedCertificationNumber == null) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    if (!storedCertificationNumber.equals(certificationNumber)) {
      return false;
    }

    // Redis 에 UserId 를 키값으로 인증 완료 여부를 저장. (유효시간 1시간으로 설정)
    redisRepository.setData(key + ":verified", true, 1, TimeUnit.HOURS);
    // 기존에 Redis 에 UserId 를 키값으로 가지는 인증번호 데이터는 유효시간에 관계없이 삭제
    redisRepository.deleteData(key);
    return true;
  }

  /**
   * 이메일 인증을 완료했는지 여부 확인
   *
   * @param key
   * @return boolean
   */
  public boolean checkVerified(String key) {
    Object storedVerifiedData = redisRepository.getData(key);
    return storedVerifiedData != null;
  }

}
