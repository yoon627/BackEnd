package com.devonoff.domain.qnapost.controller;


import com.devonoff.domain.qnapost.dto.QnaPostDto;
import com.devonoff.domain.qnapost.dto.QnaPostRequest;
import com.devonoff.domain.qnapost.dto.QnaPostUpdateDto;
import com.devonoff.domain.qnapost.service.QnaPostService;
import com.devonoff.domain.user.entity.User;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna-posts")
public class QnaPostController {

  private final QnaPostService qnaPostService;

  /**
   * 질의 응답 게시물 생성
   *
   * @param qnaPostRequest
   * @return ResponseEntity<Map < String, String>>
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, String>> createQnaPost(
      @ModelAttribute QnaPostRequest qnaPostRequest,
      @AuthenticationPrincipal User user)
  {
    return ResponseEntity.ok(qnaPostService.createQnaPost(qnaPostRequest, user));
  }
  /**
   * 질의 응답 게시물 전체 목록 조회 (최신순)
   *
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  @GetMapping
  public Page<QnaPostDto> getQnaPostList(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "") String search
  ) {
    return qnaPostService.getQnaPostList(page, search);
  }

  /**
   * 특정 사용자가 작성한 질의 응답 게시물 목록 조회 (최신순)
   *
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  @GetMapping("/author/{userId}")
  public Page<QnaPostDto> getQnaPostByUserIdList(
      @PathVariable Long userId,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "") String search
  ) {
    return qnaPostService.getQnaPostByUserIdList(userId, page, search);
  }

  /**
   * 특정 질의 응답 게시글 상세 조회
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  @GetMapping("/{qnaPostId}")
  public QnaPostDto getQnaPost(
      @PathVariable Long qnaPostId
  ) {
    return qnaPostService.getQnaPost(qnaPostId);
  }

  /**
   * 특정 질의 응답 게시글 수정
   *
   * @param qnaPostId
   * @param QnaPostUpdateDto
   * @return QnaPostDto
   */
  @PostMapping(value = "/{qnaPostId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public QnaPostDto updateQnaPost(
      @PathVariable Long qnaPostId,
      @ModelAttribute QnaPostUpdateDto QnaPostUpdateDto
  ) {
    return qnaPostService.updateQnaPost(qnaPostId, QnaPostUpdateDto);
  }

  /**
   * 특정 질의 응답 게시글 삭제
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  @DeleteMapping("/{qnaPostId}")
  public ResponseEntity<Map<String, String>> deleteQnaPost(@PathVariable Long qnaPostId) {
    return ResponseEntity.ok(qnaPostService.deleteQnaPost(qnaPostId));
  }
}
