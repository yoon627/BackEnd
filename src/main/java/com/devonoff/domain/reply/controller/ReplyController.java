package com.devonoff.domain.reply.controller;


import com.devonoff.domain.reply.dto.ReplyRequest;
import com.devonoff.domain.reply.dto.ReplyResponse;
import com.devonoff.domain.reply.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments/{commentId}/reply")
@RequiredArgsConstructor
public class ReplyController {

  private final ReplyService replyService;

  /**
   * 대댓글 생성
   */
  @PostMapping
  public ResponseEntity<ReplyResponse> createReply(
      @PathVariable("commentId") Long commentId,
      @RequestBody ReplyRequest replyRequest) {
    ReplyResponse replyResponse = replyService.createReply(commentId, replyRequest);
    return ResponseEntity.ok(replyResponse);
  }


  /**
   * 대댓글 수정
   */
  @PutMapping("/{replyId}")
  public ResponseEntity<ReplyResponse> updateReply(
      @PathVariable("commentId") Long commentId,
      @PathVariable("replyId") Long replyId,
      @RequestBody ReplyRequest replyRequest) {
    replyService.updateReply(replyId, replyRequest);
    return ResponseEntity.ok().build();
  }

  /**
   * 대댓글 삭제
   */
  @DeleteMapping("/{replyId}")
  public ResponseEntity<Void> deleteReply(
      @PathVariable("commentId") Long commentId,
      @PathVariable("replyId") Long replyId) {
    replyService.deleteReply(replyId);
    return ResponseEntity.ok().build();
  }
}
