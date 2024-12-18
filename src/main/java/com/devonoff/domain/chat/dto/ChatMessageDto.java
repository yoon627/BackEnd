package com.devonoff.domain.chat.dto;

import com.devonoff.domain.chat.entity.ChatMessage;
import com.devonoff.domain.user.dto.UserDto;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

  private Long id;
  private UserDto user;
  private String content;
  private LocalDateTime createdAt;

  public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
    return ChatMessageDto.builder()
        .id(chatMessage.getId())
        .user(UserDto.fromEntity(chatMessage.getSender()))
        .content(chatMessage.getContent())
        .createdAt(getKoreaTime(chatMessage.getCreatedAt()))
        .build();
  }

  // UTC -> Asia/Seoul 로 시간대 변경
  private static LocalDateTime getKoreaTime(LocalDateTime createdAt) {
    ZonedDateTime utcTime = createdAt.atZone(ZoneId.of("UTC"));
    ZonedDateTime koreaTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
    return koreaTime.toLocalDateTime();
  }

}
