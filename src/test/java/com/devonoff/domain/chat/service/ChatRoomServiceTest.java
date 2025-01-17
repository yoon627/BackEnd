package com.devonoff.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devonoff.domain.chat.dto.ChatRoomDto;
import com.devonoff.domain.chat.entity.ChatRoom;
import com.devonoff.domain.chat.repository.ChatRoomRepository;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

  @InjectMocks
  private ChatRoomService chatRoomService;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private StudyRepository studyRepository;

  @Mock
  private StudentRepository studentRepository;

  @Mock
  private AuthService authService;

  @Test
  @DisplayName("채팅방 생성 및 조회 - 성공 (해당 스터디에 채팅방이 존재하지 않는 경우)")
  void testGetOrCreateChatRoom_Success() {
    // given
    Long studyId = 1L;
    Long userId = 1L;
    User user = User.builder().id(2L).build();
    Study study = Study.builder().id(1L).studyName("Test Study").studyLeader(user).build();
    ChatRoom chatRoom = ChatRoom.builder().id(1L).studyName("Test Study").study(study).build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(studyRepository.findById(eq(studyId))).willReturn(Optional.of(study));
    given(studentRepository.existsByUserIdAndStudyId(eq(userId), eq(studyId))).willReturn(true);
    given(chatRoomRepository.findByStudyId(eq(studyId))).willReturn(Optional.empty());
    given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

    // when
    ChatRoomDto chatRoomDto = chatRoomService.getOrCreateChatRoom(studyId, userId);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(studyRepository, times(1)).findById(eq(studyId));
    verify(studentRepository, times(1))
        .existsByUserIdAndStudyId(eq(userId), eq(studyId));
    verify(chatRoomRepository, times(1)).findByStudyId(eq(studyId));
    verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));

    assertThat(chatRoomDto.getChatRoomId()).isEqualTo(1L);
    assertThat(chatRoomDto.getStudyId()).isEqualTo(1L);
    assertThat(chatRoomDto.getStudyName()).isEqualTo("Test Study");
    assertThat(chatRoomDto.getLeaderId()).isEqualTo(2L);
  }

  @Test
  @DisplayName("채팅방 생성 및 조회 - 성공 (해당 스터디에 채팅방이 존재하는 경우)")
  void testGetOrCreateChatRoom_Success_ExistsChatRoom() {
    // given
    Long studyId = 1L;
    Long userId = 1L;
    User user = User.builder().id(2L).build();
    Study study = Study.builder().id(1L).studyName("Test Study").studyLeader(user).build();
    ChatRoom chatRoom = ChatRoom.builder().id(1L).studyName("Test Study").study(study).build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(studyRepository.findById(eq(studyId))).willReturn(Optional.of(study));
    given(studentRepository.existsByUserIdAndStudyId(eq(userId), eq(studyId))).willReturn(true);
    given(chatRoomRepository.findByStudyId(eq(studyId))).willReturn(Optional.of(chatRoom));

    // when
    ChatRoomDto chatRoomDto = chatRoomService.getOrCreateChatRoom(studyId, userId);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(studyRepository, times(1)).findById(eq(studyId));
    verify(studentRepository, times(1))
        .existsByUserIdAndStudyId(eq(userId), eq(studyId));
    verify(chatRoomRepository, times(1)).findByStudyId(eq(studyId));

    assertThat(chatRoomDto.getChatRoomId()).isEqualTo(1L);
    assertThat(chatRoomDto.getStudyId()).isEqualTo(1L);
    assertThat(chatRoomDto.getStudyName()).isEqualTo("Test Study");
    assertThat(chatRoomDto.getLeaderId()).isEqualTo(2L);
  }

  @Test
  @DisplayName("채팅방 생성 및 조회 - 실패 (로그인한 유저와 불일치)")
  void testGetOrCreateChatRoom_Fail_UnAuthorizedUser() {
    // given
    Long studyId = 1L;
    Long userId = 1L;

    given(authService.getLoginUserId()).willReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> chatRoomService.getOrCreateChatRoom(studyId, userId));

    // then
    verify(authService, times(1)).getLoginUserId();

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_USER);
    assertThat(customException.getErrorMessage()).isEqualTo("로그인 된 사용자와 일치하지 않습니다.");
  }

  @Test
  @DisplayName("채팅방 생성 및 조회 - 실패 (존재하지 않는 스터디)")
  void testGetOrCreateChatRoom_Fail_StudyNotFound() {
    // given
    Long studyId = 1L;
    Long userId = 1L;

    given(authService.getLoginUserId()).willReturn(1L);
    given(studyRepository.findById(eq(studyId))).willReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> chatRoomService.getOrCreateChatRoom(studyId, userId));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(studyRepository, times(1)).findById(eq(studyId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.STUDY_NOT_FOUND);
    assertThat(customException.getErrorMessage()).isEqualTo("스터디를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("채팅방 생성 및 조회 - 실패 (해당 스터디 참여자가 아닌 경우)")
  void testGetOrCreateChatRoom_Fail_DoesNotStudentOfStudy() {
    // given
    Long studyId = 1L;
    Long userId = 1L;
    Study study = Study.builder().id(1L).studyName("Test Study").build();

    given(authService.getLoginUserId()).willReturn(1L);
    given(studyRepository.findById(eq(studyId))).willReturn(Optional.of(study));
    given(studentRepository.existsByUserIdAndStudyId(eq(userId), eq(studyId))).willReturn(false);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> chatRoomService.getOrCreateChatRoom(studyId, userId));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(studyRepository, times(1)).findById(eq(studyId));
    verify(studentRepository, times(1))
        .existsByUserIdAndStudyId(eq(userId), eq(studyId));

    assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.DOES_NOT_STUDENT_OF_STUDY);
    assertThat(customException.getErrorMessage()).isEqualTo("해당 스터디 참가자가 아닙니다.");
  }
}