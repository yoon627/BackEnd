package com.devonoff.domain.comment.service;

import com.devonoff.domain.comment.dto.CommentRequest;
import com.devonoff.domain.comment.dto.CommentResponse;
import com.devonoff.domain.comment.dto.CommentUpdateRequest;
import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.qnapost.repository.QnaPostRepository;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  private final StudyPostRepository studyPostRepository;
  private final QnaPostRepository qnaPostRepository;
  private final InfoSharePostRepository infoSharePostRepository;
  private final Map<PostType, Object> repositoryMap = new HashMap<>();

  // PostType에 따른 repository를 관리하는 Map
  @PostConstruct
  public void initRepositoryMap() {
    repositoryMap.put(PostType.STUDY, studyPostRepository);
    repositoryMap.put(PostType.QNA, qnaPostRepository);
    repositoryMap.put(PostType.INFO, infoSharePostRepository);
  }

  // 로그인된 사용자 ID 가져오기
  private Long extractUserIdFromPrincipal() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof UserDetails userDetails) {
      return Long.parseLong(userDetails.getUsername());
    } else if (principal instanceof String username && !username.equals("anonymousUser")) {
      return Long.parseLong(username);
    }
    throw new CustomException(ErrorCode.USER_NOT_FOUND, "로그인이 필요합니다.");
  }

  // 게시글 존재 여부 확인
  public void validatePostExists(PostType postType, Long postId) {
    Object repository = repositoryMap.get(postType);
    if (repository == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 게시글 타입입니다.");
    }

    boolean exists = false;
    if (repository instanceof StudyPostRepository) {
      exists = ((StudyPostRepository) repository).findById(postId).isPresent();
    } else if (repository instanceof QnaPostRepository) {
      exists = ((QnaPostRepository) repository).findById(postId).isPresent();
    } else if (repository instanceof InfoSharePostRepository) {
      exists = ((InfoSharePostRepository) repository).findById(postId).isPresent();
    }

    if (!exists) {
      throw new CustomException(ErrorCode.POST_NOT_FOUND, "게시글이 존재하지 않습니다.");
    }
  }

  @Transactional
  public CommentResponse createComment(CommentRequest commentRequest) {
    // 게시글 존재 여부 검증
    validatePostExists(commentRequest.getPostType(), commentRequest.getPostId());

    // 인증된 사용자 아이디 가져오기
    Long userId = extractUserIdFromPrincipal();

    // 사용자 정보 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // Comment 엔티티 생성 및 저장
    Comment comment = commentRequest.toEntity(user);
    Comment savedComment = commentRepository.save(comment);

    // 저장된 엔티티를 CommentResponse로 변환하여 반환
    return CommentResponse.fromEntity(savedComment);
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getComments(Long postId, PostType postType, Pageable pageable) {
    // 댓글 페이징 조회
    Page<Comment> comments = commentRepository.findByPostIdAndPostType(postId, postType, pageable);

    // Comment -> CommentResponse 변환
    return comments.map(CommentResponse::fromEntity);
  }

  @Transactional
  public void updateComment(Long commentId, CommentUpdateRequest commentUpdateRequest) {
    // 인증된 사용자 아이디 가져오기
    Long userId = extractUserIdFromPrincipal();

    // 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 댓글 조회
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다."));

    // 작성자 확인
    if (!comment.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    // 수정
    comment.setContent(commentUpdateRequest.getContent());
    comment.setIsSecret(commentUpdateRequest.getIsSecret());
    commentRepository.save(comment);
  }

  @Transactional
  public void deleteComment(Long commentId) {
    Long userId = extractUserIdFromPrincipal();

    // 사용자 정보 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 댓글 조회
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다."));

    // 작성자 확인
    if (!comment.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, "삭제 권한이 없습니다.");
    }

    // 댓글 삭제
    commentRepository.delete(comment);
  }
}