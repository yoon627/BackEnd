package com.devonoff.domain.videoconference.websocket;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserSessionManager {

  private final ConcurrentHashMap<String, String> userSessions = new ConcurrentHashMap<>();

  // 사용자 세션 추가
  public void addUser(String roomId, String sessionId, String camKey) {
    userSessions.put(sessionId, roomId);
    log.info("after connection UserSessionManager : " + userSessions);
  }

  // 사용자 세션 제거
  public void removeUser(String roomId, String sessionId) {
    userSessions.remove(sessionId);
    log.info("after disconnection UserSessionManager : " + userSessions);
  }

  // camKey 조회
  public String getCamKey(String sessionId) {
    return userSessions.get(sessionId);
  }

  public String getRoomId(String sessionId) {
    return userSessions.get(sessionId);
  }
}
