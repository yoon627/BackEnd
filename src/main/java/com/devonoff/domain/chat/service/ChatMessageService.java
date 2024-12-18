package com.devonoff.domain.chat.service;

import com.devonoff.domain.chat.dto.ChatMessageDto;
import com.devonoff.domain.chat.dto.ChatMessageRequest;
import com.devonoff.domain.chat.entity.ChatMessage;
import com.devonoff.domain.chat.entity.ChatRoom;
import com.devonoff.domain.chat.repository.ChatMessageRepository;
import com.devonoff.domain.chat.repository.ChatRoomRepository;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final StudentRepository studentRepository;
  private final UserRepository userRepository;
  private final AuthService authService;

  /**
   * 채팅 메시지 저장
   *
   * @param chatRoomId
   * @param chatMessageRequest
   * @return ChatMessageDto
   */
  public ChatMessageDto createChatMessage(Long chatRoomId, ChatMessageRequest chatMessageRequest) {
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

    Long senderId = chatMessageRequest.getSenderId();

    Boolean isExistsStudent = studentRepository.existsByUserIdAndStudyId(
        senderId, chatRoom.getStudy().getId());
    if (!isExistsStudent) {
      throw new CustomException(ErrorCode.DOES_NOT_STUDENT_OF_STUDY);
    }

    User user = userRepository.findById(senderId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    return ChatMessageDto.fromEntity(
        chatMessageRepository.save(
            ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .content(chatMessageRequest.getContent())
                .createdAt(LocalDateTime.now())
                .build()
        )
    );
  }

  /**
   * 특정 채팅방의 채팅 메시지 조회
   *
   * @param chatRoomId
   * @return List<ChatMessageDto>
   */
  public List<ChatMessageDto> getChatMessages(Long chatRoomId) {
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

    Long loginUserId = authService.getLoginUserId();

    Boolean isExistsStudent = studentRepository.existsByUserIdAndStudyId(
        loginUserId, chatRoom.getStudy().getId());
    if (!isExistsStudent) {
      throw new CustomException(ErrorCode.DOES_NOT_STUDENT_OF_STUDY);
    }

    return chatMessageRepository.findAllByChatRoomOrderByCreatedAtAsc(chatRoom)
        .stream()
        .map(ChatMessageDto::fromEntity)
        .toList();
  }
}
