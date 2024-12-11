package com.devonoff.domain.faceCall.controller;

import com.devonoff.domain.faceCall.util.UserIdSessionManager;
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

  private final UserIdSessionManager userIdSessionManager;

  @MessageMapping("/peer/offer/{camKey}/{roomId}")
  @SendTo("/topic/peer/offer/{camKey}/{roomId}")
  public String PeerHandleOffer(@Payload String offer,
      @DestinationVariable(value = "roomId") String roomId,
      @DestinationVariable(value = "camKey") String camKey) {
    log.info("[OFFER] {} {} : {}", roomId, camKey, offer);
    return offer;
  }

  //iceCandidate 정보를 주고 받기 위한 webSocket
  //camKey : 각 요청하는 캠의 key , roomId : 룸 아이디
  @MessageMapping("/peer/iceCandidate/{camKey}/{roomId}")
  @SendTo("/topic/peer/iceCandidate/{camKey}/{roomId}")
  public String PeerHandleIceCandidate(@Payload String candidate,
      @DestinationVariable(value = "roomId") String roomId,
      @DestinationVariable(value = "camKey") String camKey) {
    log.info("[ICECANDIDATE] {} {} : {}", roomId, camKey, candidate);
    return candidate;
  }

  @MessageMapping("/peer/answer/{camKey}/{roomId}")
  @SendTo("/topic/peer/answer/{camKey}/{roomId}")
  public String PeerHandleAnswer(@Payload String answer,
      @DestinationVariable(value = "roomId") String roomId,
      @DestinationVariable(value = "camKey") String camKey) {
    log.info("[ANSWER] {} {} : {}", roomId, camKey, answer);
    return answer;
  }

  //camKey 를 받기위해 신호를 보내는 webSocket
  @MessageMapping("/call/key")
  @SendTo("/topic/call/key")
  public String callKey(@Payload String message) {
    log.info("[Key] : {}", message);
    return message;
  }

  //자신의 camKey 를 모든 연결된 세션에 보내는 webSocket
  @MessageMapping("/send/key")
  @SendTo("/topic/send/key")
  public String sendKey(@Payload String message) {
    return message;
  }

  @MessageMapping("/send/end/{roomId}/{camKey}")
  @SendTo("/topic/send/end/{roomId}/{camKey}")
  public String endCallSignal(@Payload String message,
      @DestinationVariable(value = "roomId") String roomId,
      @DestinationVariable(value = "camKey") String camKey) {
    log.info("[END CALL] {} {} : {}", roomId, camKey, message);
    return message;
  }
}
