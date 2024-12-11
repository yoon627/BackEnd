package com.devonoff.domain.faceCall.util;

import com.devonoff.domain.study.repository.StudyRepository;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomManager {

  private final ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> roomSessions = new ConcurrentHashMap<>();
  private final StudyRepository studyRepository;
  private final TimeManager timeManager;

  // 방마다 사용자 세션 추가
  public void addUser(String roomId, String userId, String sessionId) {
    if (roomSessions.containsKey(roomId)) {
      manageRoom(roomId, userId, sessionId);
    } else {
      roomSessions.put(roomId, new ConcurrentHashMap<>());
      manageRoom(roomId, userId, sessionId);
    }
    log.info("after connection RoomManager : " + roomSessions);
  }

  // 사용자 세션 제거
  public void removeUser(String roomId, String userId, String sessionId) {
    if (studyRepository.findById(Long.valueOf(roomId)).get().getTotalParticipants()
        == roomSessions.get(roomId).size()) {
      timeManager.endTimer(roomId);
    }
    roomSessions.get(roomId).get(userId).remove(sessionId);

    if (roomSessions.get(roomId).get(userId).isEmpty()) {
      roomSessions.remove(userId);
    }
    if (roomSessions.get(roomId).isEmpty()) {
      roomSessions.remove(roomId);
    }

    log.info("after disconnection RoomManager : " + roomSessions);
  }

  private void manageRoom(String roomId, String userId, String sessionId) {
    if (roomSessions.get(roomId).containsKey(userId)) {
      roomSessions.get(roomId).get(userId).put(sessionId, "test");
    } else {
      roomSessions.get(roomId).put(userId, new ConcurrentHashMap<>());
      roomSessions.get(roomId).get(userId).put(sessionId, "test");
      if (studyRepository.findById(Long.valueOf(roomId)).get().getTotalParticipants()
          == roomSessions.get(roomId).size()) {
        timeManager.startTimer(roomId);
      }
    }
  }
}