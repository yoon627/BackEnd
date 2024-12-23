package com.devonoff.domain.studyPost.controller;

import com.devonoff.domain.studyPost.dto.StudyCommentDto;
import com.devonoff.domain.studyPost.dto.StudyCommentRequest;
import com.devonoff.domain.studyPost.dto.StudyCommentResponse;
import com.devonoff.domain.studyPost.dto.StudyPostCreateRequest;
import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.dto.StudyPostUpdateRequest;
import com.devonoff.domain.studyPost.dto.StudyReplyDto;
import com.devonoff.domain.studyPost.dto.StudyReplyRequest;
import com.devonoff.domain.studyPost.service.StudyPostService;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySubject;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-posts")
public class StudyPostController {

  private final StudyPostService studyPostService;

  // 스터디 모집글 상세 조회
  @GetMapping("/{studyPostId}")
  public ResponseEntity<StudyPostDto> getStudyPostDetail(@PathVariable Long studyPostId) {
    StudyPostDto studyPostDto = studyPostService.getStudyPostDetail(studyPostId);
    return ResponseEntity.ok(studyPostDto);
  }

  // 스터디 모집글 상세 조회(userId)
  @GetMapping("/author/{userId}")
  public ResponseEntity<Page<StudyPostDto>> getStudyPostsByUserId(
      @PathVariable Long userId, Pageable pageable) {
    Page<StudyPostDto> studyPosts = studyPostService.getStudyPostsByUserId(userId, pageable);
    return ResponseEntity.ok(studyPosts);
  }

  // 스터디 모집글 검색
  @GetMapping("/search")
  public ResponseEntity<Page<StudyPostDto>> searchStudyPosts(
      @RequestParam(required = false) StudyMeetingType meetingType,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) StudySubject subject,
      @RequestParam(required = false) StudyDifficulty difficulty,
      @RequestParam(required = false, defaultValue = "0") int dayType,
      @RequestParam(required = false) StudyPostStatus status,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) Double longitude,
      Pageable pageable) {

    Page<StudyPostDto> studyPosts = studyPostService.searchStudyPosts(
        meetingType, title, subject, difficulty, dayType, status, latitude, longitude, pageable);
    return ResponseEntity.ok(studyPosts);
  }

  // 스터디 모집글 생성
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<StudyPostDto> createStudyPost(
      @ModelAttribute StudyPostCreateRequest request) {
    StudyPostDto response = studyPostService.createStudyPost(request);
    return ResponseEntity.ok(response);
  }

  // 스터디 모집글 수정
  @PostMapping(path = "/{studyPostId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<StudyPostDto> updateStudyPost(
      @PathVariable Long studyPostId,
      @ModelAttribute StudyPostUpdateRequest request) {
    StudyPostDto response = studyPostService.updateStudyPost(studyPostId, request);
    return ResponseEntity.ok(response);
  }

  // 스터디 모집글 마감
  @PatchMapping("/{studyPostId}/close")
  public ResponseEntity<Void> closeStudyPost(@PathVariable Long studyPostId) {
    studyPostService.closeStudyPost(studyPostId);
    return ResponseEntity.ok().build();
  }

  // 스터디 모집글 모집 취소
  @PatchMapping("/{studyPostId}/cancel")
  public ResponseEntity<Void> cancelStudyPost(@PathVariable Long studyPostId) {
    studyPostService.cancelStudyPost(studyPostId);
    return ResponseEntity.ok().build();
  }

  // 모집 취소된 스터디 모집 기간 연장
  @PatchMapping("/{studyPostId}/extend-canceled")
  public ResponseEntity<Void> extendCanceledStudy(
      @PathVariable Long studyPostId, @RequestParam LocalDate recruitmentPeriod) {
    studyPostService.extendCanceledStudy(studyPostId, recruitmentPeriod);
    return ResponseEntity.ok().build();
  }

  // 댓글

  /**
   * 댓글 작성
   *
   * @param studyPostId
   * @param studyCommentRequest
   * @return ResponseEntity<StudyCommentDto>
   */
  @PostMapping("/{studyPostId}/comments")
  public ResponseEntity<StudyCommentDto> createStudyPostComment(
      @PathVariable Long studyPostId,
      @RequestBody @Valid StudyCommentRequest studyCommentRequest
  ) {
    return ResponseEntity.ok(
        studyPostService.createStudyPostComment(studyPostId, studyCommentRequest)
    );
  }

  /**
   * 댓글 조회
   *
   * @param studyPostId
   * @param page
   * @return ResponseEntity<Page < StudyCommentResponse>>
   */
  @GetMapping("/{studyPostId}/comments")
  public ResponseEntity<Page<StudyCommentResponse>> getStudyPostComments(
      @PathVariable Long studyPostId,
      @RequestParam(required = false, defaultValue = "0") Integer page
  ) {
    return ResponseEntity.ok(studyPostService.getStudyPostComments(studyPostId, page));
  }

  /**
   * 댓글 수정
   *
   * @param commentId
   * @param studyCommentRequest
   * @return ResponseEntity<StudyCommentDto>
   */
  @PutMapping("/comments/{commentId}")
  public ResponseEntity<StudyCommentDto> updateStudyPostComment(
      @PathVariable Long commentId,
      @RequestBody @Valid StudyCommentRequest studyCommentRequest
  ) {
    return ResponseEntity.ok(
        studyPostService.updateStudyPostComment(commentId, studyCommentRequest));
  }

  /**
   * 댓글 삭제
   *
   * @param commentId
   * @return ResponseEntity<StudyCommentDto>
   */
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<StudyCommentDto> deleteStudyPostComment(
      @PathVariable Long commentId
  ) {
    StudyCommentDto studyCommentDto = studyPostService.deleteStudyPostComment(commentId);
    return ResponseEntity.ok().build();
  }

  // 대댓글

  /**
   * 대댓글 작성
   *
   * @param commentId
   * @param studyReplyRequest
   * @return ResponseEntity<StudyReplyDto>
   */
  @PostMapping("/comments/{commentId}")
  public ResponseEntity<StudyReplyDto> createStudyPostReply(
      @PathVariable Long commentId,
      @RequestBody @Valid StudyReplyRequest studyReplyRequest
  ) {
    return ResponseEntity.ok(studyPostService.createStudyPostReply(commentId, studyReplyRequest));
  }

  /**
   * 대댓글 수정
   *
   * @param replyId
   * @param studyReplyRequest
   * @return ResponseEntity<StudyReplyDto>
   */
  @PutMapping("/replies/{replyId}")
  public ResponseEntity<StudyReplyDto> updateStudyPostReply(
      @PathVariable Long replyId,
      @RequestBody @Valid StudyReplyRequest studyReplyRequest
  ) {
    return ResponseEntity.ok(studyPostService.updateStudyPostReply(replyId, studyReplyRequest));
  }

  /**
   * 대댓글 삭제
   *
   * @param replyId
   * @return ResponseEntity<StudyReplyDto>
   */
  @DeleteMapping("/replies/{replyId}")
  public ResponseEntity<StudyReplyDto> deleteStudyPostReply(
      @PathVariable Long replyId
  ) {
    StudyReplyDto studyReplyDto = studyPostService.deleteStudyPostReply(replyId);
    return ResponseEntity.ok().build();
  }
}