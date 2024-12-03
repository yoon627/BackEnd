package com.devonoff.domain.comment.controller;

import com.devonoff.domain.comment.dto.CommentDto;
import com.devonoff.domain.comment.dto.CommentRequest;
import com.devonoff.domain.comment.dto.CommentResponse;
import com.devonoff.domain.comment.service.CommentService;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.type.PostType;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  private final AuthService authService;


  @PostMapping
  public ResponseEntity<CommentResponse> createComment(@RequestBody @Valid CommentRequest request) {
    Long userId = authService.getLoginUserId(); // 로그인된 사용자 ID 가져오기
    CommentResponse createdComment = commentService.createComment(request, userId);
    return ResponseEntity.ok(createdComment); // 생성된 댓글 응답 반환
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
    Long userId = authService.getLoginUserId();
    return ResponseEntity.ok(
        commentService.updateComment(commentId, dto.getContent(), dto.getIsSecret(), userId));


  }

  // 댓글 삭제
  @DeleteMapping("/{commentId}")
  public ResponseEntity<String> deleteComment(@PathVariable("commentId") Long commentId) {
    Long userId = authService.getLoginUserId();
    commentService.deleteComment(commentId, userId);
    return ResponseEntity.ok().build();
  }


}