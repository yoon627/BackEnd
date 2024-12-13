package com.devonoff.domain.infosharepost.service;

import static com.devonoff.type.ErrorCode.POST_NOT_FOUND;
import static com.devonoff.type.ErrorCode.UNAUTHORIZED_ACCESS;
import static com.devonoff.type.ErrorCode.USER_NOT_FOUND;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.reply.Repository.ReplyRepository;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.PostType;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoSharePostService {

  private final InfoSharePostRepository infoSharePostRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final ReplyRepository replyRepository;
  private final PhotoService photoService;
  private final AuthService authService;

  @Value("${spring.data.web.pageable.default-page-size}")
  private Integer defaultPageSize;

  @Value("${cloud.aws.s3.default-thumbnail-image-url}")
  private String defaultThumbnailImageUrl;

  @Transactional
  public InfoSharePostDto createInfoSharePost(InfoSharePostDto infoSharePostDto) {
    User user = this.userRepository.findById(authService.getLoginUserId())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    infoSharePostDto.setUser(UserDto.fromEntity(user));
    MultipartFile file = infoSharePostDto.getFile();
    if (file != null && !file.isEmpty()) {
      infoSharePostDto.setThumbnailImgUrl(photoService.save(file));
    } else {
      infoSharePostDto.setThumbnailImgUrl(defaultThumbnailImageUrl);
    }

    return InfoSharePostDto.fromEntity(
        this.infoSharePostRepository.save(InfoSharePostDto.toEntity(infoSharePostDto)));
  }

  public Page<InfoSharePostDto> getInfoSharePosts(Integer page, String search) {
    Pageable pageable = PageRequest.of(page, defaultPageSize, Sort.by("createdAt").descending());
    return this.infoSharePostRepository.findAllByTitleContaining(search,
        pageable).map(InfoSharePostDto::fromEntity);
  }

  public Page<InfoSharePostDto> getInfoSharePostsByUserId(Long userId, Integer page,
      String search) {
    Pageable pageable = PageRequest.of(page, defaultPageSize, Sort.by("createdAt").descending());
    return this.infoSharePostRepository.findAllByUserIdAndTitleContaining(userId, search, pageable)
        .map(InfoSharePostDto::fromEntity);
  }

  public InfoSharePostDto getInfoSharePostByPostId(Long infoPostId) {
    return InfoSharePostDto.fromEntity(this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND)));
  }

  @Transactional
  public InfoSharePostDto updateInfoSharePost(Long infoPostId, InfoSharePostDto infoSharePostDto) {
    if (!Objects.equals(authService.getLoginUserId(), infoSharePostDto.getUserId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }
    InfoSharePost infoSharePost = this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    MultipartFile file = infoSharePostDto.getFile();
    String originImgUrl = infoSharePost.getThumbnailImgUrl();
    String requestImgUrl = infoSharePostDto.getThumbnailImgUrl();

    if (file != null && !file.isEmpty()) {
      photoService.delete(originImgUrl);
      infoSharePost.setThumbnailImgUrl(photoService.save(file));
    } else {
      if (requestImgUrl != null && !requestImgUrl.isEmpty() && requestImgUrl.equals(
          defaultThumbnailImageUrl)) {
        photoService.delete(originImgUrl);
        infoSharePost.setThumbnailImgUrl(defaultThumbnailImageUrl);
      }
    }

    infoSharePost.setTitle(infoSharePostDto.getTitle());
    infoSharePost.setDescription(infoSharePostDto.getDescription());
    return InfoSharePostDto.fromEntity(this.infoSharePostRepository.save(infoSharePost));
  }

  @Transactional
  public void deleteInfoSharePost(Long infoPostId) {
    InfoSharePost infoSharePost = this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    if (!Objects.equals(authService.getLoginUserId(), infoSharePost.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }
    photoService.delete(infoSharePost.getThumbnailImgUrl());
    List<Comment> commentList = commentRepository.findAllByPostIdAndPostType(infoPostId,
        PostType.INFO);

    for (Comment comment : commentList) {
      replyRepository.deleteAllByComment(comment);
    }

    commentRepository.deleteAllByPostIdAndPostType(infoPostId, PostType.INFO);
    this.infoSharePostRepository.deleteById(infoPostId);
  }
}