package com.devonoff.domain.qnapost.controller;


import com.devonoff.domain.qnapost.dto.PublicQnaPostDto;
import com.devonoff.domain.qnapost.dto.QnaPostDto;
import com.devonoff.domain.qnapost.dto.QnaPostRequest;
import com.devonoff.domain.qnapost.dto.QnaPostUpdateDto;
import com.devonoff.domain.qnapost.service.QnaPostService;
import com.devonoff.domain.user.entity.User;
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
  public ResponseEntity<Void> createQnaPost(@ModelAttribute QnaPostRequest qnaPostRequest) {
    return qnaPostService.createQnaPost(qnaPostRequest); // 서비스 호출 및 상태 코드 반환
  }

  /**
   * 질의 응답 게시물 전체 목록 조회 (최신순)
   *
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  @GetMapping
  public ResponseEntity<Page<PublicQnaPostDto>> getQnaPostList(
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @RequestParam(name = "search", required = false, defaultValue = "") String search
  ) {
    return ResponseEntity.ok(qnaPostService.getQnaPostList(page, search));

  }

  /**
   * 특정 사용자가 작성한 질의 응답 게시물 목록 조회 (최신순)
   *
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  @GetMapping("/author/{userId}")
  public ResponseEntity<Page<PublicQnaPostDto>> getQnaPostByUserIdList(
      @PathVariable(name = "userId") Long userId,
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @RequestParam(name = "search", required = false, defaultValue = "") String search
  ) {
    return ResponseEntity.ok(qnaPostService.getQnaPostByUserIdList(userId, page, search));
  }

  /**
   * 특정 질의 응답 게시글 상세 조회
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  @GetMapping("/{qnaPostId}")
  public ResponseEntity<QnaPostDto> getQnaPost(@PathVariable Long qnaPostId) {
    return ResponseEntity.ok(qnaPostService.getQnaPost(qnaPostId));
  }

  /**
   * 특정 질의 응답 게시글 수정
   *
   * @param qnaPostId
   * @param user
   * @param QnaPostUpdateDto
   * @return QnaPostDto
   */
  @PostMapping(value = "/{qnaPostId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<QnaPostDto> updateQnaPost(
      @PathVariable("qnaPostId") Long qnaPostId,
      @ModelAttribute QnaPostUpdateDto QnaPostUpdateDto,
      @AuthenticationPrincipal User user

  ) {
    return ResponseEntity.ok(qnaPostService.updateQnaPost(qnaPostId, QnaPostUpdateDto, user));
  }

  /**
   * 특정 질의 응답 게시글 삭제
   *
   * @param qnaPostId
   * @param user
   * @return QnaPostDto
   */
  @DeleteMapping("/{qnaPostId}")
  public ResponseEntity<Void> deleteQnaPost(
      @PathVariable("qnaPostId") Long qnaPostId,
      @AuthenticationPrincipal User user
  ) {
    qnaPostService.deleteQnaPost(qnaPostId, user);
    return ResponseEntity.ok().build();
  }
}
