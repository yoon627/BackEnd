package com.devonoff.domain.videoconference.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventListener {

  private final UserSessionManager userSessionManager;
  private final StudyRoomManager studyRoomManager;

  public WebSocketEventListener(UserSessionManager userSessionManager,
      StudyRoomManager studyRoomManager) {
    this.userSessionManager = userSessionManager;
    this.studyRoomManager = studyRoomManager;
  }

  @EventListener
  public void handleSessionConnect(SessionConnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

    // 연결된 사용자 정보 가져오기
    String sessionId = headerAccessor.getSessionId();
    String camKey = (String) headerAccessor.getNativeHeader("camKey")
        .get(0); // 클라이언트에서 헤더로 camKey 전달
    String roomId = (String) headerAccessor.getNativeHeader("roomId").get(0);

    // 세션 관리에 추가
    userSessionManager.addUser(roomId, sessionId, camKey);
    studyRoomManager.addUser(roomId, sessionId, camKey);
    log.info("Connected sessionId: {}, camKey: {}, roomId: {}", sessionId, camKey, roomId);

    // 필요 시 연결된 사용자 정보로 추가 작업 수행
  }

  @EventListener
  public void handleSessionDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    // 연결 종료된 사용자 정보 가져오기
    String sessionId = headerAccessor.getSessionId();
    String roomId = userSessionManager.getRoomId(sessionId);
    String camKey = studyRoomManager.getCamKey(roomId, sessionId);
    userSessionManager.removeUser(roomId, sessionId);
    studyRoomManager.removeUser(roomId, sessionId);
    log.info("Disconnected sessionId: {}, camKey: {}, roomId: {}", sessionId, camKey, roomId);

    // 필요 시 연결 종료 알림 전송
    if (camKey != null && roomId != null) {
      log.info("Disconnection : Notifying roomId: {} about camKey: {}", roomId, camKey);
      // 연결된 사용자에게 camKey 종료 알림을 보낼 수 있음
    }
  }
}