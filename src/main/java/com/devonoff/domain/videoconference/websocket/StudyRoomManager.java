package com.devonoff.domain.videoconference.websocket;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StudyRoomManager {

  private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> roomSessions = new ConcurrentHashMap<>();

  // 방마다 사용자 세션 추가
  public void addUser(String roomId, String sessionId, String camKey) {
    //TODO 인원이 다차면 방에 시작 시간을 적어야함
    if (roomSessions.containsKey(roomId)) {
      roomSessions.get(roomId).put(sessionId, camKey);
    } else {
      roomSessions.put(roomId, new ConcurrentHashMap<>());
      roomSessions.get(roomId).put(sessionId, camKey);
    }

    log.info("after connection RoomManager : " + roomSessions);
  }

  // 사용자 세션 제거
  public void removeUser(String roomId, String sessionId) {
    if (roomSessions.containsKey(roomId)) {
      roomSessions.get(roomId).remove(sessionId);
      if (roomSessions.get(roomId).isEmpty()) {
        roomSessions.remove(roomId);
      }
    }
    log.info("after disconnection RoomManager : " + roomSessions);
  }

  // camKey 조회
  public String getCamKey(String roomId, String sessionId) {
    return roomSessions.get(roomId).get(sessionId);
  }
}
