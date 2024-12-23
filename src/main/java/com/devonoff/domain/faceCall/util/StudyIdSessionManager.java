package com.devonoff.domain.faceCall.util;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StudyIdSessionManager {

  private final ConcurrentHashMap<String, String> studySessions = new ConcurrentHashMap<>();

  // 방마다 사용자 세션 추가
  public void addUser(String sessionId, String roomId) {
    studySessions.put(sessionId, roomId);
  }

  // 사용자 세션 제거
  public void removeUser(String sessionId) {
    studySessions.remove(sessionId);
  }

  public String getStudyId(String sessionId) {
    return studySessions.get(sessionId);
  }
}

