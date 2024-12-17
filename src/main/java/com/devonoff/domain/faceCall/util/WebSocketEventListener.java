package com.devonoff.domain.faceCall.util;

import com.devonoff.domain.user.repository.UserRepository;
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

  private final UserIdSessionManager userIdSessionManager;
  private final RoomManager roomManager;
  private final StudyIdSessionManager studyIdSessionManager;
  private final UserRepository userRepository;

  @EventListener
  public void handleSessionConnect(SessionConnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    // 연결된 사용자 정보 가져오기
    String sessionId = headerAccessor.getSessionId();
    String nickname = String.valueOf(
        headerAccessor.getNativeHeader("nickname").get(0)); // TODO 필요하면 쓰기
    String studyId = String.valueOf(headerAccessor.getNativeHeader("studyId").get(0));
    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    System.out.println("nickname: " + nickname);
    System.out.println("studyId: " + studyId);
    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    String userId = String.valueOf(userRepository.findByNickname(nickname).get().getId());
    //TODO 프론트엔드에서 jwt 보내줘야함 안보내주면 contextholder나 다른 방법 찾아야함
//    String jwt = (String) headerAccessor.getNativeHeader("jwt").get(0);
//    String userId = String.valueOf(jwtProvider.getUserId(jwt));
    userIdSessionManager.addUser(sessionId, userId);
    studyIdSessionManager.addUser(sessionId, studyId);
    roomManager.addUser(studyId, userId, sessionId);
  }

  @EventListener
  public void handleSessionDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    log.info("Disconnected sessionId: {}", headerAccessor.getSessionId());
    // 연결 종료된 사용자 정보 가져오기
    String sessionId = headerAccessor.getSessionId();
    String roomId = studyIdSessionManager.getStudyId(sessionId);
    String userId = userIdSessionManager.getUserId(sessionId);
    roomManager.removeUser(roomId, userId, sessionId);
    studyIdSessionManager.removeUser(sessionId);
    userIdSessionManager.removeUser(sessionId);
  }
}
