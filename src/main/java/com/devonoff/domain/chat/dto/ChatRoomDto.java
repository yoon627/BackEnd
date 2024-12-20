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

  public static ChatRoomDto fromEntity(ChatRoom chatRoom) {
    return ChatRoomDto.builder()
        .chatRoomId(chatRoom.getId())
        .studyId(chatRoom.getStudy().getId())
        .studyName(chatRoom.getStudyName())
        .build();
  }

}
