package com.devonoff.domain.qnapost.service;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.qnapost.dto.PublicQnaPostDto;
import com.devonoff.domain.qnapost.dto.QnaPostDto;
import com.devonoff.domain.qnapost.dto.QnaPostRequest;
import com.devonoff.domain.qnapost.dto.QnaPostUpdateDto;
import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.domain.qnapost.repository.QnaPostRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
      QnaPostRequest qnaPostRequest, User user) {
    // 입력값 검증
    if (qnaPostRequest.getTitle() == null || qnaPostRequest.getTitle().isBlank()) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }
    if (qnaPostRequest.getContent() == null || qnaPostRequest.getContent().isBlank()) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }
    user = userRepository.findByEmail(user.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    String uploadedThumbnailUrl = photoService.save(qnaPostRequest.getThumbnail());

    qnaPostRepository.save(
        QnaPost.builder()
            .title(qnaPostRequest.getTitle())
            .content(qnaPostRequest.getContent())
            .thumbnailUrl(uploadedThumbnailUrl)
            .postType(PostType.QNA_POST) //타입설정
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
  public Page<PublicQnaPostDto> getQnaPostList(Integer page, String search) {

    Sort sort = Sort.by(Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(page - 1, QNA_PAGE_SIZE, sort);

    // search가 비어있는 경우 전체 게시물 조회
    if (search == null || search.isBlank()) {
      return qnaPostRepository.findAll(pageable)
          .map(PublicQnaPostDto::fromEntity);
    }
    return qnaPostRepository.findByTitleContaining(search, pageable)
        .map(PublicQnaPostDto::fromEntity);
  }

  /**
   * 특정 사용자의 질의 응답 게시글 목록 조회 (최신순)
   *
   * @param userId
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  public Page<PublicQnaPostDto> getQnaPostByUserIdList(Long userId, Integer page, String search) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    Sort sort = Sort.by(Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(page - 1, QNA_PAGE_SIZE, sort);

    return qnaPostRepository.findByUserAndTitleContaining(user, search, pageable)
        .map(PublicQnaPostDto::fromEntity);
  }

  /**
   * 특정 질의 응답 게시글 상세 조회
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  public com.devonoff.domain.qnapost.dto.QnaPostDto getQnaPost(Long qnaPostId) {
    return QnaPostDto.fromEntity(
        qnaPostRepository.findById(qnaPostId).orElseThrow(() -> new CustomException(
            ErrorCode.POST_NOT_FOUND)));
  }

  /**
   * 특정 질의 응답 게시글 수정
   *
   * @param qnaPostId
   * @param user
   * @param qnaPostUpdateDto
   * @return QnaPostDto
   */
  @Transactional
  public QnaPostDto updateQnaPost(Long qnaPostId,
      QnaPostUpdateDto qnaPostUpdateDto, User user) {

    // TO DO 토큰에서 유저 확인 후 수정 작업
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    // 작성자 확인
    if (!qnaPost.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    photoService.delete(qnaPost.getThumbnailUrl());
    String uploadedThumbnailUrl = photoService.save(qnaPostUpdateDto.getThumbnail());

    qnaPost.setThumbnailUrl(uploadedThumbnailUrl);
    qnaPost.setTitle(qnaPostUpdateDto.getTitle());
    qnaPost.setContent(qnaPostUpdateDto.getContent());

    return QnaPostDto.fromEntity(qnaPost);
  }

  /**
   * 특정 질의 응답 게시글 삭제
   *
   * @param qnaPostId
   * @param user
   * @return QnaPostDto
   */
  @Transactional
  public Map<String, String> deleteQnaPost(Long qnaPostId, User user) {

    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    // 게시글 작성자 확인
    if (!qnaPost.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    if (qnaPost.getThumbnailUrl() != null) {
      photoService.delete(qnaPost.getThumbnailUrl());

    }
    qnaPostRepository.delete(qnaPost);

    Map<String, String> responseMap = new HashMap<>();
    responseMap.put("message", "정상적으로 삭제 되었습니다.");

    return responseMap;

  }
}



