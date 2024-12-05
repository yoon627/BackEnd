package com.devonoff.domain.comment.controller;

import com.devonoff.domain.comment.dto.CommentRequest;
import com.devonoff.domain.comment.dto.CommentResponse;
import com.devonoff.domain.comment.service.CommentService;
import com.devonoff.type.PostType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {


  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @RequestBody @Valid CommentRequest commentRequest) {
    // 댓글 생성 로직은 서비스에서 처리
    CommentResponse response = commentService.createComment(commentRequest);

    return ResponseEntity.ok(response);
  }


  @GetMapping
  public ResponseEntity<Page<CommentResponse>> getComments(
      @RequestParam("post_id") Long post_id,
      @RequestParam(value = "page",defaultValue = "1") Integer page,
      @RequestParam ("post_type")String post_type) {

    // 페이지 번호를 0부터 시작하도록 변환
    Pageable pageable = PageRequest.of(page - 1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));

    // 댓글 조회
    Page<CommentResponse> response = commentService.getComments(post_id, PostType.valueOf(post_type.toUpperCase()), pageable);
    return ResponseEntity.ok(response);
  }

  /**
   * 댓글 삭제
   *
   * @param commentId 삭제할 댓글의 ID
   * @return 상태 코드만 반환
   */
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId) {
    commentService.deleteComment(commentId);
    return ResponseEntity.ok().build();
  }


}