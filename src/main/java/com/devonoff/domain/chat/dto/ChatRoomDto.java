package com.devonoff.domain.chat.dto;

import com.devonoff.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {

  private Long chatRoomId;
  private Long studyId;
  private String studyName;
  private Long leaderId;

  public static ChatRoomDto fromEntity(ChatRoom chatRoom, Long leaderId) {
    return ChatRoomDto.builder()
        .chatRoomId(chatRoom.getId())
        .studyId(chatRoom.getStudy().getId())
        .studyName(chatRoom.getStudyName())
        .leaderId(leaderId)
        .build();
  }

}
