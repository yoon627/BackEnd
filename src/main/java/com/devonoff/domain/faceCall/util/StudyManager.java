package com.devonoff.domain.faceCall.util;

import com.devonoff.domain.study.repository.StudyRepository;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyManager {

  private static final ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> studySessions = new ConcurrentHashMap<>();
  private final StudyRepository studyRepository;
  private final TimeManager timeManager;

  public static ArrayList<String> getStudyMembers(String studyId) {
    return new ArrayList<>(studySessions.get(studyId).keySet());
  }

  // 방마다 사용자 세션 추가
  public void addUser(String roomId, String userId, String sessionId) {
    if (studySessions.containsKey(roomId)) {
      manageRoom(roomId, userId, sessionId);
    } else {
      studySessions.put(roomId, new ConcurrentHashMap<>());
      manageRoom(roomId, userId, sessionId);
    }
  }

  // 사용자 세션 제거
  public void removeUser(String roomId, String userId, String sessionId) {
    if (studyRepository.findById(Long.valueOf(roomId)).get().getTotalParticipants()
        == studySessions.get(roomId).size()) {
      timeManager.endTimer(roomId);
    }
    studySessions.get(roomId).get(userId).remove(sessionId);

    if (studySessions.get(roomId).get(userId).isEmpty()) {
      studySessions.remove(userId);
    }
    if (studySessions.get(roomId).isEmpty()) {
      studySessions.remove(roomId);
    }

  }

  private void manageRoom(String roomId, String userId, String sessionId) {
    if (studySessions.get(roomId).containsKey(userId)) {
      studySessions.get(roomId).get(userId).put(sessionId, "test");
    } else {
      studySessions.get(roomId).put(userId, new ConcurrentHashMap<>());
      studySessions.get(roomId).get(userId).put(sessionId, "test");
      if (studyRepository.findById(Long.valueOf(roomId)).get().getTotalParticipants()
          == studySessions.get(roomId).size()) {
        timeManager.startTimer(roomId);
      }
    }
  }
}