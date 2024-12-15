package com.devonoff.domain.qnapost.controller;


import com.devonoff.domain.qnapost.dto.QnaCommentDto;
import com.devonoff.domain.qnapost.dto.QnaCommentRequest;
import com.devonoff.domain.qnapost.dto.QnaCommentResponse;
import com.devonoff.domain.qnapost.dto.QnaPostDto;
import com.devonoff.domain.qnapost.dto.QnaReplyDto;
import com.devonoff.domain.qnapost.dto.QnaReplyRequest;
import com.devonoff.domain.qnapost.dto.QnaPostRequest;
import com.devonoff.domain.qnapost.dto.QnaPostUpdateDto;
import com.devonoff.domain.qnapost.service.QnaPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/qna-posts")
@RequiredArgsConstructor
public class QnaPostController {

  private final QnaPostService qnaPostService;

  /**
   * 질의 응답 게시글 생성
   *
   * @param qnaPostRequest 게시글 요청 데이터
   * @return HTTP 상태코드
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> createQnaPost(@ModelAttribute @Valid QnaPostRequest qnaPostRequest) {
    // `author` 필드에서 이메일 정보 추출
    String email = qnaPostRequest.getAuthor();

    // 서비스 호출
    return qnaPostService.createQnaPost(qnaPostRequest, email);
  }

  /**
   * 게시글 수정
   *
   * @param qnaPostId        게시글 ID
   * @param qnaPostUpdateDto 수정 요청 데이터
   * @return 수정된 게시글 정보
   */
  @PostMapping(value = "/{qnaPostId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<QnaPostDto> updateQnaPost(
      @PathVariable("qnaPostId") Long qnaPostId,
      @ModelAttribute QnaPostUpdateDto qnaPostUpdateDto) {

    QnaPostDto updatedPost = qnaPostService.updateQnaPost(qnaPostId, qnaPostUpdateDto);
    return ResponseEntity.ok(updatedPost);
  }

  /**
   * 질의 응답 게시글 목록 조회
   *
   * @param pageable 페이지 번호
   * @param search   검색어
   * @return 페이징된 게시글 목록
   */
  @GetMapping
  public ResponseEntity<Page<QnaPostDto>> getQnaPostList(
      Pageable pageable,
      @RequestParam(name = "search", required = false) String search) {
    return ResponseEntity.ok(qnaPostService.getQnaPostList(pageable, search));
  }

  /**
   * 특정 사용자의 질의 응답 게시글 목록 조회 토큰O
   *
   * @param userId   사용자 ID
   * @param pageable 페이지 번호
   * @param search   검색어
   * @return 페이징된 게시글 목록
   */
  @GetMapping("/author/{userId}")
  public ResponseEntity<Page<QnaPostDto>> getQnaPostByUserIdList(
      @PathVariable("userId") Long userId,
      Pageable pageable,
      @RequestParam(name = "search", required = false) String search) {
    Page<QnaPostDto> response = qnaPostService.getQnaPostByUserIdList(userId, pageable, search);
    return ResponseEntity.ok(response);
  }

  /**
   * 특정 질의 응답 게시글 상세 조회 토큰 X
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  @GetMapping("/{qnaPostId}")
  public ResponseEntity<QnaPostDto> getQnaPost(@PathVariable("qnaPostId") Long qnaPostId) {
    QnaPostDto response = qnaPostService.getQnaPost(qnaPostId);
    return ResponseEntity.ok(response);
  }

  /**
   * 게시글 삭제
   *
   * @param qnaPostId 게시글 ID
   * @return HTTP 상태코드
   */
  @DeleteMapping("/{qnaPostId}")
  public ResponseEntity<Void> deleteQnaPost(
      @PathVariable("qnaPostId") Long qnaPostId) {
    // 서비스 호출
    qnaPostService.deleteQnaPost(qnaPostId);
    return ResponseEntity.ok().build(); // 상태 코드 200 반환
  }

  // 댓글
  /**
   * 댓글 작성
   *
   * @param qnaPostId
   * @param qnaCommentRequest
   * @return ResponseEntity<QnaCommentDto>
   */
  @PostMapping("/{qnaPostId}/comments")
  public ResponseEntity<QnaCommentDto> createQnaPostComment(
      @PathVariable Long qnaPostId,
      @RequestBody @Valid QnaCommentRequest qnaCommentRequest
  ) {
    return ResponseEntity.ok(qnaPostService.createQnaPostComment(qnaPostId, qnaCommentRequest));
  }

  /**
   * 댓글 조회
   *
   * @param qnaPostId
   * @param page
   * @return ResponseEntity<Page<QnaCommentResponse>>
   */
  @GetMapping("/{qnaPostId}/comments")
  public ResponseEntity<Page<QnaCommentResponse>> getQnaPostComments(
      @PathVariable Long qnaPostId,
      @RequestParam(required = false, defaultValue = "0") Integer page
  ) {
    return ResponseEntity.ok(qnaPostService.getQnaPostComments(qnaPostId, page));
  }

  /**
   * 댓글 수정
   *
   * @param commentId
   * @param qnaCommentRequest
   * @return ResponseEntity<QnaCommentDto>
   */
  @PutMapping("/comments/{commentId}")
  public ResponseEntity<QnaCommentDto> updateQnaPostComment(
      @PathVariable Long commentId,
      @RequestBody @Valid QnaCommentRequest qnaCommentRequest
  ) {
    return ResponseEntity.ok(qnaPostService.updateQnaPostComment(commentId, qnaCommentRequest));
  }

  /**
   * 댓글 삭제
   *
   * @param commentId
   * @return ResponseEntity<QnaCommentDto>
   */
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<QnaCommentDto> deleteQnaPostComment(
      @PathVariable Long commentId
  ) {
    QnaCommentDto qnaCommentDto = qnaPostService.deleteQnaPostComment(commentId);
    return ResponseEntity.ok().build();
  }

  // 대댓글
  /**
   * 대댓글 작성
   *
   * @param commentId
   * @param qnaReplyRequest
   * @return ResponseEntity<QnaReplyDto>
   */
  @PostMapping("/comments/{commentId}")
  public ResponseEntity<QnaReplyDto> createQnaPostReply(
      @PathVariable Long commentId,
      @RequestBody @Valid QnaReplyRequest qnaReplyRequest
  ) {
    return ResponseEntity.ok(qnaPostService.createQnaPostReply(commentId, qnaReplyRequest));
  }

  /**
   * 대댓글 수정
   *
   * @param replyId
   * @param qnaReplyRequest
   * @return ResponseEntity<QnaReplyDto>
   */
  @PutMapping("/replies/{replyId}")
  public ResponseEntity<QnaReplyDto> updateQnaPostReply(
      @PathVariable Long replyId,
      @RequestBody @Valid QnaReplyRequest qnaReplyRequest
  ) {
    return ResponseEntity.ok(qnaPostService.updateQnaPostReply(replyId, qnaReplyRequest));
  }

  /**
   * 대댓글 삭제
   *
   * @param replyId
   * @return ResponseEntity<QnaReplyDto>
   */
  @DeleteMapping("/replies/{replyId}")
  public ResponseEntity<QnaReplyDto> deleteQnaPostReply(
      @PathVariable Long replyId
  ) {
    QnaReplyDto qnaReplyDto = qnaPostService.deleteQnaPostReply(replyId);
    return ResponseEntity.ok().build();
  }
}