package com.devonoff.domain.redis.repository;

import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthRedisRepository {

  private final RedisTemplate<String, String> redisTemplate;

  /**
   * Redis 에 key, value 데이터 저장시 유효 시간 설정해서 저장
   *
   * @param key
   * @param value
   * @param timeout
   * @param unit
   */
  public void setData(String key, String value, long timeout, TimeUnit unit) {
    try {

      redisTemplate.opsForValue().set(key, value, timeout, unit);

    } catch (Exception e) {
      e.printStackTrace();
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Redis 에 key 에 해당하는 데이터 조회
   *
   * @param key
   * @return Object
   */
  public String getData(String key) {
    try {

      return redisTemplate.opsForValue().get(key);

    } catch (Exception e) {
      e.printStackTrace();
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Redis 에 key 에 해당하는 데이터 삭제
   *
   * @param key
   */
  public void deleteData(String key) {
    try {

      redisTemplate.delete(key);

    } catch (Exception e) {
      e.printStackTrace();
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
