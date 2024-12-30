package com.devonoff.domain.faceCall.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

  private final StringRedisTemplate stringRedisTemplate;

  public void setAlarm(String studyId, long durationSeconds) {
    ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
    String key = "Alarm:" + studyId;

    valueOperations.set(key, "faceCall ends after " + durationSeconds + " seconds", durationSeconds,
        TimeUnit.SECONDS);
  }

  public void setEnd(String studyId, long durationSeconds) {
    ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
    String key = "End:" + studyId;

    valueOperations.set(key, "faceCall ends after " + durationSeconds + " seconds", durationSeconds,
        TimeUnit.SECONDS);
  }

  public boolean isAlarmPresent(String studyId) {
    String key = "End:" + studyId;
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
  }
}
