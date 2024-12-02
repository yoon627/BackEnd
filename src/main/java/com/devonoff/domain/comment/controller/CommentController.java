package com.devonoff.domain.comment.controller;

import com.devonoff.domain.comment.dto.CommentDto;
import com.devonoff.domain.comment.service.CommentService;
import com.devonoff.domain.user.entity.User;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  // 댓글 생성
  @PostMapping
  public ResponseEntity<CommentDto> createComment(@RequestBody CommentDto dto) {
    User user = getLoggedInUser(); // 로그인된 사용자 가져오기
    return ResponseEntity.ok(commentService.createComment(dto, user));
  }

  // 특정 게시글의 댓글 조회
  @GetMapping
  public ResponseEntity<List<CommentDto>> getCommentsByPost(
      @RequestParam(name = "postId") Long postId,
      @RequestParam(name = "postType") PostType postType) {
    return ResponseEntity.ok(commentService.getCommentsByPost(postId, postType));
  }

  // 댓글 수정
  @PutMapping("/{commentId}")
  public ResponseEntity<CommentDto> updateComment(
      @PathVariable("commentId") Long commentId,
      @RequestBody CommentDto dto) {
    User user = getLoggedInUser();
    return ResponseEntity.ok(
        commentService.updateComment(commentId, dto.getContent(), dto.getIsSecret(), user));
  }

  // 댓글 삭제
  @DeleteMapping("/{commentId}")
  public ResponseEntity<String> deleteComment(@PathVariable("commentId") Long commentId) {
    User user = getLoggedInUser();
    commentService.deleteComment(commentId, user);
    //return ResponseEntity.noContent().build();
    return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다."); // 성공 메시지 반환
  }

  // 로그인된 사용자 정보 가져오기
  private User getLoggedInUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
      return (User) principal;
    }
    throw new CustomException(ErrorCode.USER_NOT_FOUND);
  }
}