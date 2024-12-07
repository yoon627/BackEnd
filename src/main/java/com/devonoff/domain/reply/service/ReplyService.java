
package com.devonoff.domain.reply.service;


import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.comment.service.CommentService;
import com.devonoff.domain.reply.Repository.ReplyRepository;
import com.devonoff.domain.reply.dto.ReplyRequest;
import com.devonoff.domain.reply.dto.ReplyResponse;
import com.devonoff.domain.reply.entity.Reply;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyService {

  private final ReplyRepository replyRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final CommentService commentService;

  // 로그인된 사용자 ID 가져오기
  private Long extractUserIdFromPrincipal() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof String) {
      return Long.parseLong((String) principal);
    } else if (principal instanceof UserDetails) {
      return Long.parseLong(((UserDetails) principal).getUsername());
    } else {
      throw new CustomException(ErrorCode.USER_NOT_FOUND, "로그인된 사용자만 접근 가능합니다.");
    }
  }

  // 대댓글 생성
  @Transactional
  public ReplyResponse createReply(Long commentId, ReplyRequest replyRequest) {
    // 부모 댓글 존재 여부 확인
    Comment parentComment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND, "부모 댓글을 찾을 수 없습니다."));

    // 로그인된 사용자 ID 가져오기
    Long userId = extractUserIdFromPrincipal();

    // 사용자 조회
    User loggedInUser = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "작성자를 찾을 수 없습니다."));

    // ReplyRequest -> Reply 엔티티 변환
    Reply reply = replyRequest.toEntity(loggedInUser, parentComment);

    // 대댓글 저장
    Reply savedReply = replyRepository.save(reply);

    // 저장된 대댓글을 응답 DTO로 변환하여 반환
    return ReplyResponse.fromEntity(savedReply);
  }

  @Transactional
  public ReplyResponse updateReply(Long replyId, ReplyRequest replyRequest) {
    // 수정할 대댓글 존재 여부 확인
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND, "대댓글을 찾을 수 없습니다."));

    // 로그인된 사용자 ID 가져오기
    Long userId = extractUserIdFromPrincipal();

    // 작성자 권한 검증
    if (!reply.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, "수정 권한이 없습니다.");
    }

    // 대댓글 내용 및 비밀 여부 수정
    reply.setContent(replyRequest.getContent());
    reply.setIsSecret(replyRequest.isSecret());

    // 수정된 대댓글 저장 및 반환
    Reply updatedReply = replyRepository.save(reply);
    return ReplyResponse.fromEntity(updatedReply);
  }

  @Transactional
  public void deleteReply(Long replyId) {
    // 삭제할 대댓글 존재 여부 확인
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND, "대댓글을 찾을 수 없습니다."));

    // 로그인된 사용자 ID 가져오기
    Long userId = extractUserIdFromPrincipal();

    // 작성자 권한 검증
    if (!reply.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, "삭제 권한이 없습니다.");
    }

    // 대댓글 삭제
    replyRepository.delete(reply);
  }






}
