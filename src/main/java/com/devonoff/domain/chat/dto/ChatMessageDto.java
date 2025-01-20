package com.devonoff.domain.chat.dto;

import com.devonoff.domain.chat.entity.ChatMessage;
import com.devonoff.domain.user.dto.UserDto;
import java.time.LocalDateTime;
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
        .createdAt(chatMessage.getCreatedAt())
        .build();
  }

}
