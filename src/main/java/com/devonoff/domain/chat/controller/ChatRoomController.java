package com.devonoff.domain.chat.controller;

import com.devonoff.domain.chat.dto.ChatMessageDto;
import com.devonoff.domain.chat.dto.ChatRoomDto;
import com.devonoff.domain.chat.service.ChatMessageService;
import com.devonoff.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;

  /**
   * 채팅방 생성 및 조회
   *
   * @param studyId
   * @param userId
   * @return ResponseEntity<ChatRoomDto>
   */
  @PostMapping("/study/{studyId}/participant/{userId}")
  public ResponseEntity<ChatRoomDto> enterChatRoom(
      @PathVariable Long studyId, @PathVariable Long userId
  ) {
   return ResponseEntity.ok(chatRoomService.getOrCreateChatRoom(studyId, userId));
  }

  /**
   * 특정 채팅방의 채팅 메시지 조회
   *
   * @param chatRoomId
   * @return ResponseEntity<List<ChatMessageDto>>
   */
  @GetMapping("/{chatRoomId}/messages")
  public ResponseEntity<Page<ChatMessageDto>> getChatMessages(
      @PathVariable Long chatRoomId,
      @RequestParam(required = false, defaultValue = "0") Integer page
  ) {
    return ResponseEntity.ok(chatMessageService.getChatMessages(chatRoomId, page));
  }

}
