package com.devonoff.domain.chat.controller;

import com.devonoff.domain.chat.dto.ChatMessageDto;
import com.devonoff.domain.chat.dto.ChatMessageRequest;
import com.devonoff.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final ChatMessageService chatMessageService;

  @MessageMapping("/chat/{chatRoomId}/send-messages")
  @SendTo("/topic/chat/{chatRoomId}")
  public ChatMessageDto sendMessage(
      @DestinationVariable Long chatRoomId,
      @Payload ChatMessageRequest chatMessageRequest
  ) {
    return chatMessageService.createChatMessage(chatRoomId, chatMessageRequest);
  }

}
