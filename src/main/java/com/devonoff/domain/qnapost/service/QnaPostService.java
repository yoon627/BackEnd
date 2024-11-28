package com.devonoff.domain.qnapost.service;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.domain.qnapost.repository.QnaPostRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QnaPostService {

  private static final int QNA_PAGE_SIZE = 5;

  private final QnaPostRepository qnaPostRepository;
  private final UserRepository userRepository;

  private final PhotoService photoService;

  /**
   * 질의 응답 게시글 생성
   *
   * @param qnaPostRequest
   * @return Map<String, String>
   */
  @Transactional
  public Map<String, String> createQnaPost(
      com.devonoff.domain.qnapost.dto.QnaPostRequest qnaPostRequest) {

    User user = userRepository.findByEmail(qnaPostRequest.getAuthor())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String uploadedThumbnailUrl = photoService.save(qnaPostRequest.getThumbnail());

    qnaPostRepository.save(
        QnaPost.builder()
            .title(qnaPostRequest.getTitle())
            .content(qnaPostRequest.getContent())
            .thumbnailUrl(uploadedThumbnailUrl)
            .user(user)
            .build()
    );

    Map<String, String> responseMap = new HashMap<>();
    responseMap.put("message", "게시글 작성이 완료되었습니다.");

    return responseMap;
  }

  /**
   * 질의 응답 게시글 전체 목록 조회 (최신순)
   *
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  public Page<com.devonoff.domain.qnapost.dto.QnaPostDto> getQnaPostList(Integer page, String search) {
    // TO DO 토큰에서 유저 확인 후 생성 작업
    Sort sort = Sort.by(Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(page - 1, QNA_PAGE_SIZE, sort);

    return qnaPostRepository.findByTitleContaining(search, pageable)
        .map(com.devonoff.domain.qnapost.dto.QnaPostDto::fromEntity);
  }

  /**
   * 특정 사용자의 질의 응답 게시글 목록 조회 (최신순)
   *
   * @param userId
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  public Page<com.devonoff.domain.qnapost.dto.QnaPostDto> getQnaPostByUserIdList(Long userId, Integer page, String search) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    Sort sort = Sort.by(Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(page - 1, QNA_PAGE_SIZE, sort);

    return qnaPostRepository.findByUserAndTitleContaining(user, search, pageable)
        .map(com.devonoff.domain.qnapost.dto.QnaPostDto::fromEntity);
  }

  /**
   * 특정 질의 응답 게시글 상세 조회
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  public com.devonoff.domain.qnapost.dto.QnaPostDto getQnaPost(Long qnaPostId) {
    return com.devonoff.domain.qnapost.dto.QnaPostDto.fromEntity(
        qnaPostRepository.findById(qnaPostId).orElseThrow(() -> new CustomException(
            ErrorCode.POST_NOT_FOUND)));
  }

  /**
   * 특정 질의 응답 게시글 수정
   *
   * @param qnaPostId
   * @param qnaPostUpdateDto
   * @return QnaPostDto
   */
  @Transactional
  public com.devonoff.domain.qnapost.dto.QnaPostDto updateQnaPost(Long qnaPostId, com.devonoff.domain.qnapost.dto.QnaPostUpdateDto qnaPostUpdateDto) {
    // TO DO 토큰에서 유저 확인 후 수정 작업
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    photoService.delete(qnaPost.getThumbnailUrl());
    String uploadedThumbnailUrl = photoService.save(qnaPostUpdateDto.getThumbnail());

    qnaPost.setThumbnailUrl(uploadedThumbnailUrl);
    qnaPost.setTitle(qnaPostUpdateDto.getTitle());
    qnaPost.setContent(qnaPostUpdateDto.getContent());

    return com.devonoff.domain.qnapost.dto.QnaPostDto.fromEntity(qnaPost);
  }

  /**
   * 특정 질의 응답 게시글 삭제
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  @Transactional
  public Map<String, String> deleteQnaPost(Long qnaPostId) {
    // TO DO 토큰에서 유저 확인 후 삭제 작업
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    photoService.delete(qnaPost.getThumbnailUrl());
    qnaPostRepository.delete(qnaPost);

    Map<String, String> responseMap = new HashMap<>();
    responseMap.put("message", "정상적으로 삭제 되었습니다.");

    return responseMap;
  }

}
