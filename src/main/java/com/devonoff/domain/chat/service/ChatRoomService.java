package com.devonoff.domain.chat.service;

import com.devonoff.domain.chat.dto.ChatRoomDto;
import com.devonoff.domain.chat.entity.ChatRoom;
import com.devonoff.domain.chat.repository.ChatRoomRepository;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;
  private final StudyRepository studyRepository;
  private final StudentRepository studentRepository;

  private final AuthService authService;

  /**
   * 특정 스터디의 채팅방이 있으면 Get 없으면 Create 해서 반환
   *
   * @param studyId
   * @param userId
   * @return ChatRoomDto
   */
  public ChatRoomDto getOrCreateChatRoom(Long studyId, Long userId) {
    Long loginUserId = authService.getLoginUserId();
    if (!loginUserId.equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
    }

    Study study = studyRepository.findById(studyId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

    Boolean isExistsStudent = studentRepository.existsByUserIdAndStudyId(userId, studyId);
    if (!isExistsStudent) {
      throw new CustomException(ErrorCode.DOES_NOT_STUDENT_OF_STUDY);
    }

    ChatRoom chatRoom = chatRoomRepository.findByStudyId(studyId)
        .orElseGet(() -> chatRoomRepository.save(
            ChatRoom.builder()
                .studyName(study.getStudyName())
                .study(study)
                .build())
        );

    return ChatRoomDto.fromEntity(chatRoom, study.getStudyLeader().getId());
  }
}
