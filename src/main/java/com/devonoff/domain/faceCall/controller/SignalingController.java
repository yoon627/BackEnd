package com.devonoff.domain.faceCall.controller;

import com.devonoff.domain.faceCall.util.NicknameSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SignalingController {

  private final NicknameSessionManager nicknameSessionManager;

  @MessageMapping("/peer/offer/{nickname}/{studyId}")
  @SendTo("/topic/peer/offer/{nickname}/{studyId}")
  public String PeerHandleOffer(@Payload String offer,
      @DestinationVariable(value = "studyId") String studyId,
      @DestinationVariable(value = "nickname") String nickname) {
    return offer;
  }

  //iceCandidate 정보를 주고 받기 위한 webSocket
  //nickname : 각 요청하는 캠의 key , studyId : 룸 아이디
  @MessageMapping("/peer/iceCandidate/{nickname}/{studyId}")
  @SendTo("/topic/peer/iceCandidate/{nickname}/{studyId}")
  public String PeerHandleIceCandidate(@Payload String candidate,
      @DestinationVariable(value = "studyId") String studyId,
      @DestinationVariable(value = "nickname") String nickname) {
    return candidate;
  }

  @MessageMapping("/peer/answer/{nickname}/{studyId}")
  @SendTo("/topic/peer/answer/{nickname}/{studyId}")
  public String PeerHandleAnswer(@Payload String answer,
      @DestinationVariable(value = "studyId") String studyId,
      @DestinationVariable(value = "nickname") String nickname) {
    return answer;
  }

  //camKey 를 받기위해 신호를 보내는 webSocket
  @MessageMapping("/call/key")
  @SendTo("/topic/call/key")
  public String callKey(@Payload String message) {
    return message;
  }

  //자신의 camKey 를 모든 연결된 세션에 보내는 webSocket
  @MessageMapping("/send/key")
  @SendTo("/topic/send/key")
  public String sendKey(@Payload String message) {
    return message;
  }

  @MessageMapping("/send/end/{studyId}/{nickname}")
  @SendTo("/topic/send/end/{studyId}/{nickname}")
  public String endCallSignal(@Payload String message,
      @DestinationVariable(value = "studyId") String studyId,
      @DestinationVariable(value = "nickname") String nickname) {
    return message;
  }
}
