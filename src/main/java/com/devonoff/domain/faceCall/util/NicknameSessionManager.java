package com.devonoff.domain.faceCall.util;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NicknameSessionManager {

  private final ConcurrentHashMap<String, String> userSessions = new ConcurrentHashMap<>();

  // 사용자 세션 추가
  public void addUser(String sessionId, String userId) {
    userSessions.put(sessionId, userId);
  }

  // 사용자 세션 제거
  public void removeUser(String sessionId) {
    userSessions.remove(sessionId);
  }

  public String getNickname(String sessionId) {
    return userSessions.get(sessionId);
  }
}

