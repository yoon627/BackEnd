package com.devonoff.domain.chat.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.chat.dto.ChatMessageDto;
import com.devonoff.domain.chat.dto.ChatRoomDto;
import com.devonoff.domain.chat.entity.ChatMessage;
import com.devonoff.domain.chat.entity.ChatRoom;
import com.devonoff.domain.chat.service.ChatMessageService;
import com.devonoff.domain.chat.service.ChatRoomService;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.user.entity.User;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.util.JwtProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatRoomController.class)
@Import(SecurityConfig.class) // SecurityConfig를 명시적으로 포함 (Optional)
@AutoConfigureMockMvc(addFilters = false)
class ChatRoomControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  JwtProvider jwtProvider;

  @MockBean
  private ChatRoomService chatRoomService;

  @MockBean
  private ChatMessageService chatMessageService;

  @Test
  @DisplayName("채팅방 생성 및 조회 - 성공")
  void testEnterChatRoom_Success() throws Exception {
    // Given
    Long studyId = 1L;
    Long userId = 1L;
    ChatRoomDto chatRoomDto =
        ChatRoomDto.builder().chatRoomId(1L).studyId(1L).studyName("Test Study").build();

    Mockito.when(chatRoomService.getOrCreateChatRoom(studyId, userId)).thenReturn(chatRoomDto);

    // When & Then
    mockMvc.perform(post("/api/chat/study/{studyId}/participant/{userId}", studyId, userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("채팅방 생성 및 조회 - 실패 (로그인한 유저와 불일치)")
  void testEnterChatRoom_Fail_UnAuthorizedUser() throws Exception {
    // Given
    Long studyId = 1L;
    Long userId = 1L;

    Mockito.when(chatRoomService.getOrCreateChatRoom(studyId, userId))
        .thenThrow(new CustomException(ErrorCode.UNAUTHORIZED_USER));

    // When & Then
    mockMvc.perform(post("/api/chat/study/{studyId}/participant/{userId}", studyId, userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("채팅방 생성 및 조회 - 실패 (존재하지 않는 스터디)")
  void testEnterChatRoom_Fail_DoesNotStudentOfStudy() throws Exception {
    // Given
    Long studyId = 1L;
    Long userId = 1L;

    Mockito.when(chatRoomService.getOrCreateChatRoom(studyId, userId))
        .thenThrow(new CustomException(ErrorCode.DOES_NOT_STUDENT_OF_STUDY));

    // When & Then
    mockMvc.perform(post("/api/chat/study/{studyId}/participant/{userId}", studyId, userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("채팅방 생성 및 조회 - 실패 (해당 스터디 참여자가 아닌 경우)")
  void testEnterChatRoom_Fail_StudyNotFound() throws Exception {
    // Given
    Long studyId = 1L;
    Long userId = 1L;

    Mockito.when(chatRoomService.getOrCreateChatRoom(studyId, userId))
        .thenThrow(new CustomException(ErrorCode.STUDY_NOT_FOUND));

    // When & Then
    mockMvc.perform(post("/api/chat/study/{studyId}/participant/{userId}", studyId, userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("채팅 메시지 내역 조회 - 성공")
  void testGetChatMessages_Success() throws Exception {
    // Given
    Long chatRoomId = 1L;

    Study study = Study.builder().id(1L).studyName("Test Study").build();
    ChatRoom chatRoom = ChatRoom.builder().id(1L).studyName("Test Study").study(study).build();
    User user = User.builder().id(1L).build();

    List<ChatMessage> chatMessageList = List.of(
        ChatMessage.builder()
            .id(1L)
            .content("Test Message 1")
            .createdAt(LocalDateTime.now())
            .sender(user)
            .chatRoom(chatRoom)
            .build(),
        ChatMessage.builder()
            .id(2L)
            .content("Test Message 2")
            .createdAt(LocalDateTime.now())
            .sender(user)
            .chatRoom(chatRoom)
            .build(),
        ChatMessage.builder()
            .id(3L)
            .content("Test Message 3")
            .createdAt(LocalDateTime.now())
            .sender(user)
            .chatRoom(chatRoom)
            .build()
    );

    Page<ChatMessage> chatMessagePage = new PageImpl<>(chatMessageList);
    Page<ChatMessageDto> responseChatMessages = chatMessagePage.map(ChatMessageDto::fromEntity);

    Mockito.when(chatMessageService.getChatMessages(chatRoomId, 0)).thenReturn(responseChatMessages);

    // When & Then
    mockMvc.perform(get("/api/chat/{chatRoomId}/messages", chatRoomId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("채팅 메시지 내역 조회 - 실패 (존재하지 않는 채팅방)")
  void testGetChatMessages_Fail_ChatRoomNotFound() throws Exception {
    // Given
    Long chatRoomId = 1L;

    Mockito.when(chatMessageService.getChatMessages(chatRoomId, 0))
        .thenThrow(new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

    // When & Then
    mockMvc.perform(get("/api/chat/{chatRoomId}/messages", chatRoomId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("채팅 메시지 내역 조회 - 실패 (해당 스터디 참여자가 아닌 경우)")
  void testGetChatMessages_Fail_DoesNotStudentOfStudy() throws Exception {
    // Given
    Long chatRoomId = 1L;

    Mockito.when(chatMessageService.getChatMessages(chatRoomId, 0))
        .thenThrow(new CustomException(ErrorCode.DOES_NOT_STUDENT_OF_STUDY));

    // When & Then
    mockMvc.perform(get("/api/chat/{chatRoomId}/messages", chatRoomId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

}