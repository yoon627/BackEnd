package com.devonoff.domain.infosharepost.controller;

import com.devonoff.domain.infosharepost.dto.InfoShareCommentDto;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentRequest;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentResponse;
import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.dto.InfoShareReplyDto;
import com.devonoff.domain.infosharepost.dto.InfoShareReplyRequest;
import com.devonoff.domain.infosharepost.service.InfoSharePostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info-posts")
@RequiredArgsConstructor
public class InfoSharePostController {

  private final InfoSharePostService infoSharePostService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<InfoSharePostDto> createInfoSharePost(
      @ModelAttribute InfoSharePostDto infoSharePostDto) {
    var result = this.infoSharePostService.createInfoSharePost(infoSharePostDto);
    return ResponseEntity.ok(result);
  }

  @GetMapping
  public ResponseEntity<Page<InfoSharePostDto>> getInfoSharePosts(
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "") String search) {
    var result = this.infoSharePostService.getInfoSharePosts(page, search);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/author/{userId}")
  public ResponseEntity<Page<InfoSharePostDto>> getInfoSharePostsByUserId(@PathVariable Long userId,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "") String search) {
    var result = this.infoSharePostService.getInfoSharePostsByUserId(userId, page, search);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{infoPostId}")
  public ResponseEntity<InfoSharePostDto> getInfoSharePostByPostId(
      @PathVariable Long infoPostId) {
    var result = this.infoSharePostService.getInfoSharePostByPostId(infoPostId);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/{infoPostId}")
  public ResponseEntity<InfoSharePostDto> updateInfoSharePost(@PathVariable Long infoPostId,
      @ModelAttribute InfoSharePostDto infoSharePostDto) {
    var result = this.infoSharePostService.updateInfoSharePost(infoPostId, infoSharePostDto);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{infoPostId}")
  public void deleteInfoSharePost(@PathVariable Long infoPostId) {
    this.infoSharePostService.deleteInfoSharePost(infoPostId);
  }

  // 댓글

  /**
   * 댓글 생성
   *
   * @param infoPostId
   * @param infoShareCommentRequest
   * @return ResponseEntity<InfoShareCommentDto>
   */
  @PostMapping("/{infoPostId}/comments")
  public ResponseEntity<InfoShareCommentDto> createInfoSharePostComment(
      @PathVariable Long infoPostId,
      @RequestBody @Valid InfoShareCommentRequest infoShareCommentRequest
  ) {
    return ResponseEntity.ok(
        infoSharePostService.createInfoSharePostComment(infoPostId, infoShareCommentRequest)
    );
  }

  /**
   * 댓글 조회
   *
   * @param infoPostId
   * @return ResponseEntity<Page < InfoShareCommentDto>>
   */
  @GetMapping("/{infoPostId}/comments")
  public ResponseEntity<Page<InfoShareCommentResponse>> getInfoSharePostComments(
      @PathVariable Long infoPostId,
      @RequestParam(required = false, defaultValue = "0") Integer page
  ) {
    return ResponseEntity.ok(infoSharePostService.getInfoSharePostComments(infoPostId, page));
  }

  /**
   * 댓글 수정
   *
   * @param commentId
   * @param infoShareCommentRequest
   * @return ResponseEntity<InfoShareCommentDto>
   */
  @PutMapping("/comments/{commentId}")
  public ResponseEntity<InfoShareCommentDto> updateInfoSharePostComment(
      @PathVariable Long commentId,
      @RequestBody @Valid InfoShareCommentRequest infoShareCommentRequest
  ) {
    return ResponseEntity.ok(
        infoSharePostService.updateInfoSharePostComment(commentId, infoShareCommentRequest)
    );
  }

  /**
   * 댓글 삭제
   *
   * @param commentId
   * @return ResponseEntity<InfoShareCommentDto>
   */
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<InfoShareCommentDto> deleteInfoSharePostComment(
      @PathVariable Long commentId
  ) {
    InfoShareCommentDto infoShareCommentDto =
        infoSharePostService.deleteInfoSharePostComment(commentId);
    return ResponseEntity.ok().build();
  }

  // 대댓글

  /**
   * 대댓글 생성
   *
   * @param commentId
   * @param infoShareReplyRequest
   * @return ResponseEntity<InfoShareReplyDto>
   */
  @PostMapping("/comments/{commentId}")
  public ResponseEntity<InfoShareReplyDto> createInfoSharePostReply(
      @PathVariable Long commentId,
      @RequestBody @Valid InfoShareReplyRequest infoShareReplyRequest
  ) {
    return ResponseEntity.ok(
        infoSharePostService.createInfoSharePostReply(commentId, infoShareReplyRequest)
    );
  }

  /**
   * 대댓글 수정
   *
   * @param replyId
   * @param infoShareReplyRequest
   * @return ResponseEntity<InfoShareReplyDto>
   */
  @PutMapping("/replies/{replyId}")
  public ResponseEntity<InfoShareReplyDto> updateInfoSharePostReply(
      @PathVariable Long replyId,
      @RequestBody @Valid InfoShareReplyRequest infoShareReplyRequest
  ) {
    return ResponseEntity.ok(
        infoSharePostService.updateInfoSharePostReply(replyId, infoShareReplyRequest)
    );
  }

  /**
   * 대댓글 삭제
   *
   * @param replyId
   * @return ResponseEntity<InfoShareReplyDto>
   */
  @DeleteMapping("/replies/{replyId}")
  public ResponseEntity<InfoShareReplyDto> deleteInfoSharePostReply(
      @PathVariable Long replyId
  ) {
    InfoShareReplyDto infoShareReplyDto = infoSharePostService.deleteInfoSharePostReply(replyId);
    return ResponseEntity.ok().build();
  }
}