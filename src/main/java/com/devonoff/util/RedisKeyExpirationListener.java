package com.devonoff.util;

import com.devonoff.domain.faceCall.util.StudyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisKeyExpirationListener implements MessageListener {

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String expiredKey = message.toString();
    if (expiredKey.startsWith("Alarm:")) {
      String studyId = expiredKey.split(":")[1];
      String alarmMessage =
          "{\"type\": \"ALARM\", \"message\": \"스터디룸이 10분뒤 종료됩니다.\"}";
      for (String nickname : StudyManager.getStudyMembers(studyId)) {
        messagingTemplate.convertAndSend("/topic/alarm/" + studyId + "/" + nickname, alarmMessage);
      }
    } else if (expiredKey.startsWith("End:")) {
      String studyId = expiredKey.split(":")[1];
      String alarmMessage =
          "{\"type\": \"END\", \"message\": \"스터디룸의 종료 시간이 되었습니다.\"}";
      for (String nickname : StudyManager.getStudyMembers(studyId)) {
        messagingTemplate.convertAndSend("/topic/alarm/" + studyId + "/" + nickname, alarmMessage);
      }
    }
  }
}

