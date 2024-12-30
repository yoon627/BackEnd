package com.devonoff.domain.faceCall.util;

import com.devonoff.domain.faceCall.service.AlarmService;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.time.Duration;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

  private final NicknameSessionManager nicknameSessionManager;
  private final StudyManager studyManager;
  private final StudyIdSessionManager studyIdSessionManager;
  private final UserRepository userRepository;
  private final AlarmService alarmService;
  private final StudyRepository studyRepository;

  @EventListener
  public void handleSessionConnect(SessionConnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    // 연결된 사용자 정보 가져오기
    String sessionId = headerAccessor.getSessionId();
    String nickname = String.valueOf(
        headerAccessor.getNativeHeader("nickname").get(0));
    String studyId = String.valueOf(headerAccessor.getNativeHeader("studyId").get(0));
    nicknameSessionManager.addUser(sessionId, nickname);
    studyIdSessionManager.addUser(sessionId, studyId);
    studyManager.addUser(studyId, nickname, sessionId);
    LocalTime endTime = studyRepository.findById(Long.parseLong(studyId))
        .orElseThrow(() -> new CustomException(
            ErrorCode.STUDY_NOT_FOUND)).getEndTime();
    LocalTime now = LocalTime.now();
    long durationSeconds = Duration.between(now, endTime).toSeconds();
    if (!alarmService.isAlarmPresent(studyId)) {
      if (durationSeconds > 600) {
        alarmService.setAlarm(studyId, durationSeconds - 600);
      }
      alarmService.setEnd(studyId, durationSeconds);
    }
  }

  @EventListener
  public void handleSessionDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    // 연결 종료된 사용자 정보 가져오기
    String sessionId = headerAccessor.getSessionId();
    String studyId = studyIdSessionManager.getStudyId(sessionId);
    String nickname = nicknameSessionManager.getNickname(sessionId);
    studyManager.removeUser(studyId, nickname, sessionId);
    studyIdSessionManager.removeUser(sessionId);
    nicknameSessionManager.removeUser(sessionId);
  }
}
