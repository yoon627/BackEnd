package com.devonoff.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devonoff.domain.chat.dto.ChatMessageDto;
import com.devonoff.domain.chat.dto.ChatMessageRequest;
import com.devonoff.domain.chat.entity.ChatMessage;
import com.devonoff.domain.chat.entity.ChatRoom;
import com.devonoff.domain.chat.repository.ChatMessageRepository;
import com.devonoff.domain.chat.repository.ChatRoomRepository;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

  @InjectMocks
  private ChatMessageService chatMessageService;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private StudentRepository studentRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthService authService;

  @Test
  @DisplayName("채팅 메시지 저장 - 성공")
  void testCreateChatMessage_Success() {
    // given
    Long chatRoomId = 1L;
    Long senderId = 1L;
    ChatMessageRequest chatMessageRequest = ChatMessageRequest.builder()
        .senderId(1L)
        .content("Test Message")
        .build();

    Study study = Study.builder().id(1L).studyName("Test Study").build();
    ChatRoom chatRoom = ChatRoom.builder().id(1L).studyName("Test Study").study(study).build();
    User user = User.builder().id(1L).build();
    ChatMessage chatMessage = ChatMessage.builder()
        .id(1L)
        .chatRoom(chatRoom)
        .sender(user)
        .content("Test Message")
        .createdAt(LocalDateTime.now())
        .build();

    given(chatRoomRepository.findById(eq(chatRoomId))).willReturn(Optional.of(chatRoom));
    given(studentRepository.existsByUserIdAndStudyId(eq(senderId), eq(chatRoom.getStudy().getId())))
        .willReturn(true);
    given(userRepository.findById(eq(senderId))).willReturn(Optional.of(user));
    given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

    // when
    ChatMessageDto chatMessageDto = chatMessageService.createChatMessage(chatRoomId,
        chatMessageRequest);

    // then
    verify(chatRoomRepository, times(1)).findById(eq(chatRoomId));
    verify(studentRepository, times(1))
        .existsByUserIdAndStudyId(eq(senderId), eq(chatRoom.getStudy().getId()));
    verify(userRepository, times(1)).findById(eq(senderId));
    verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));

    assertThat(chatMessageDto.getId()).isEqualTo(1L);
    assertThat(chatMessageDto.getUser().getId()).isEqualTo(1L);
    assertThat(chatMessageDto.getContent()).isEqualTo("Test Message");
  }

  @Test
  @DisplayName("채팅 메시지 저장 - 실패 (존재하지 않는 채팅방)")
  void testCreateChatMessage_Fail_ChatRoomNotFound() {
    // given
    Long chatRoomId = 1L;
    Long senderId = 1L;
    ChatMessageRequest chatMessageRequest = ChatMessageRequest.builder()
        .senderId(1L)
        .content("Test Message")
        .build();

    given(chatRoomRepository.findById(eq(chatRoomId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> chatMessageService.createChatMessage(chatRoomId, chatMessageRequest));

    // then
    verify(chatRoomRepository, times(1)).findById(eq(chatRoomId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
    assertThat(customException.getErrorMessage()).isEqualTo("채팅방을 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("채팅 메시지 저장 - 실패 (해당 스터디 참여자가 아닌 경우)")
  void testCreateChatMessage_Fail_DoesNotStudentOfStudy() {
    // given
    Long chatRoomId = 1L;
    Long senderId = 1L;
    ChatMessageRequest chatMessageRequest = ChatMessageRequest.builder()
        .senderId(1L)
        .content("Test Message")
        .build();

    Study study = Study.builder().id(1L).studyName("Test Study").build();
    ChatRoom chatRoom = ChatRoom.builder().id(1L).studyName("Test Study").study(study).build();

    given(chatRoomRepository.findById(eq(chatRoomId))).willReturn(Optional.of(chatRoom));
    given(studentRepository.existsByUserIdAndStudyId(eq(senderId), eq(chatRoom.getStudy().getId())))
        .willReturn(false);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> chatMessageService.createChatMessage(chatRoomId, chatMessageRequest));

    // then
    verify(chatRoomRepository, times(1)).findById(eq(chatRoomId));
    verify(studentRepository, times(1))
        .existsByUserIdAndStudyId(eq(senderId), eq(chatRoom.getStudy().getId()));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.DOES_NOT_STUDENT_OF_STUDY);
    assertThat(customException.getErrorMessage()).isEqualTo("해당 스터디 참가자가 아닙니다.");
  }

  @Test
  @DisplayName("채팅 메시지 저장 - 실패 (존재하지 않는 유저)")
  void testCreateChatMessage_Fail_UserNotFound() {
    // given
    Long chatRoomId = 1L;
    Long senderId = 1L;
    ChatMessageRequest chatMessageRequest = ChatMessageRequest.builder()
        .senderId(1L)
        .content("Test Message")
        .build();

    Study study = Study.builder().id(1L).studyName("Test Study").build();
    ChatRoom chatRoom = ChatRoom.builder().id(1L).studyName("Test Study").study(study).build();

    given(chatRoomRepository.findById(eq(chatRoomId))).willReturn(Optional.of(chatRoom));
    given(studentRepository.existsByUserIdAndStudyId(eq(senderId), eq(chatRoom.getStudy().getId())))
        .willReturn(true);
    given(userRepository.findById(eq(senderId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> chatMessageService.createChatMessage(chatRoomId, chatMessageRequest));

    // then
    verify(chatRoomRepository, times(1)).findById(eq(chatRoomId));
    verify(studentRepository, times(1))
        .existsByUserIdAndStudyId(eq(senderId), eq(chatRoom.getStudy().getId()));
    verify(userRepository, times(1)).findById(eq(senderId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(customException.getErrorMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("채팅 메시지 내역 조회 - 성공")
  void testGetChatMessages_Success() {
    // given
    Long chatRoomId = 1L;
    Long loginUserId = 1L;
    Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());

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

    given(chatRoomRepository.findById(eq(chatRoomId))).willReturn(Optional.of(chatRoom));
    given(authService.getLoginUserId()).willReturn(1L);
    given(studentRepository.existsByUserIdAndStudyId(eq(loginUserId), eq(chatRoomId)))
        .willReturn(true);
    given(chatMessageRepository.findAllByChatRoom(eq(chatRoom), eq(pageable)))
        .willReturn(chatMessagePage);

    // when
    Page<ChatMessageDto> responseChatMessages = chatMessageService.getChatMessages(chatRoomId, 0);

    // then
    verify(chatRoomRepository, times(1)).findById(eq(chatRoomId));
    verify(authService, times(1)).getLoginUserId();
    verify(studentRepository, times(1))
        .existsByUserIdAndStudyId(eq(loginUserId), eq(chatRoomId));
    verify(chatMessageRepository, times(1))
        .findAllByChatRoom(eq(chatRoom), eq(pageable));

    assertThat(responseChatMessages).isNotNull();
    assertThat(responseChatMessages.getSize()).isEqualTo(3);
  }

  @Test
  @DisplayName("채팅 메시지 내역 조회 - 실패 (존재하지 않는 채팅방)")
  void testGetChatMessages_Fail_ChatRoomNotFound() {
    // given
    Long chatRoomId = 1L;

    given(chatRoomRepository.findById(eq(chatRoomId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> chatMessageService.getChatMessages(chatRoomId, 0));

    // then
    verify(chatRoomRepository, times(1)).findById(eq(chatRoomId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
    assertThat(customException.getErrorMessage()).isEqualTo("채팅방을 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("채팅 메시지 내역 조회 - 실패 (해당 스터디 참여자가 아닌 경우)")
  void testGetChatMessages_Fail_DoesNotStudentOfStudy() {
    // given
    Long chatRoomId = 1L;
    Long loginUserId = 1L;

    Study study = Study.builder().id(1L).studyName("Test Study").build();
    ChatRoom chatRoom = ChatRoom.builder().id(1L).studyName("Test Study").study(study).build();

    given(chatRoomRepository.findById(eq(chatRoomId))).willReturn(Optional.of(chatRoom));
    given(authService.getLoginUserId()).willReturn(1L);
    given(studentRepository.existsByUserIdAndStudyId(eq(loginUserId), eq(chatRoomId)))
        .willReturn(false);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> chatMessageService.getChatMessages(chatRoomId, 0));

    // then
    verify(chatRoomRepository, times(1)).findById(eq(chatRoomId));
    verify(authService, times(1)).getLoginUserId();
    verify(studentRepository, times(1))
        .existsByUserIdAndStudyId(eq(loginUserId), eq(chatRoomId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.DOES_NOT_STUDENT_OF_STUDY);
    assertThat(customException.getErrorMessage()).isEqualTo("해당 스터디 참가자가 아닙니다.");
  }
}