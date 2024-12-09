package com.devonoff.domain.infosharepost.service;

import static com.devonoff.type.ErrorCode.POST_NOT_FOUND;
import static com.devonoff.type.ErrorCode.UNAUTHORIZED_ACCESS;
import static com.devonoff.type.ErrorCode.USER_NOT_FOUND;

import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoSharePostService {

  private final InfoSharePostRepository infoSharePostRepository;
  private final UserRepository userRepository;
  private final PhotoService photoService;
  private final AuthService authService;

  public InfoSharePostDto createInfoSharePost(InfoSharePostDto infoSharePostDto,
      MultipartFile file) {
    if (!file.isEmpty()) {
      infoSharePostDto.setThumbnailImgUrl(photoService.save(file));
    }
    User user = this.userRepository.findById(authService.getLoginUserId())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    infoSharePostDto.setUserDto(UserDto.fromEntity(user));
    return InfoSharePostDto.fromEntity(
        this.infoSharePostRepository.save(InfoSharePostDto.toEntity(infoSharePostDto)));
  }

  public Page<InfoSharePostDto> getInfoSharePosts(Pageable pageable, String search) {
    return this.infoSharePostRepository.findAllByTitleContaining(search,
        pageable).map(InfoSharePostDto::fromEntity);
  }

  public Page<InfoSharePostDto> getInfoSharePostsByUserId(Long userId, Pageable pageable,
      String search) {
    return this.infoSharePostRepository.findAllByUserIdAndTitleContaining(userId, search, pageable)
        .map(InfoSharePostDto::fromEntity);
  }

  public InfoSharePostDto getInfoSharePostByPostId(Long infoPostId) {
    return InfoSharePostDto.fromEntity(this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND)));
  }

  public InfoSharePostDto updateInfoSharePost(Long infoPostId, InfoSharePostDto infoSharePostDto,
      MultipartFile file) {
    if (!file.isEmpty()) {
      infoSharePostDto.setThumbnailImgUrl(photoService.save(file));
    }
    if (!Objects.equals(authService.getLoginUserId(), infoSharePostDto.getUserDto().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }
    InfoSharePost infoSharePost = this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    infoSharePost.setThumbnailImgUrl(infoSharePostDto.getThumbnailImgUrl());
    infoSharePost.setTitle(infoSharePostDto.getTitle());
    infoSharePost.setDescription(infoSharePostDto.getDescription());
    return InfoSharePostDto.fromEntity(this.infoSharePostRepository.save(infoSharePost));
  }

  public void deleteInfoSharePost(Long infoPostId) {
    InfoSharePost infoSharePost = this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    if (!Objects.equals(authService.getLoginUserId(), infoSharePost.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }
    if (infoSharePost.getThumbnailImgUrl() != null) {
      try {
        photoService.delete(infoSharePost.getThumbnailImgUrl());
      } catch (Exception e) {
        log.info("존재하지 않는 사진입니다.");
      }
    }
    this.infoSharePostRepository.deleteById(infoPostId);
  }
}
