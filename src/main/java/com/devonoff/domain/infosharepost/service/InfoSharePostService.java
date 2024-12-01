package com.devonoff.domain.infosharepost.service;

import static com.devonoff.type.ErrorCode.POST_NOT_FOUND;
import static com.devonoff.type.ErrorCode.UNAUTHORIZED_ACCESS;
import static com.devonoff.type.ErrorCode.USER_NOT_FOUND;

import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InfoSharePostService {

  private final InfoSharePostRepository infoSharePostRepository;
  private final UserRepository userRepository;

  public InfoSharePostDto createInfoSharePost(InfoSharePostDto infoSharePostDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    infoSharePostDto.setUserId(Long.parseLong(userDetails.getUsername()));
    return InfoSharePostDto.fromEntity(
        this.infoSharePostRepository.save(InfoSharePostDto.toEntity(infoSharePostDto)));
  }

  public Page<InfoSharePostDto> getInfoSharePosts(Integer page, String search) {
    Pageable pageable = PageRequest.of(page, 10);
    return this.infoSharePostRepository.findAllByTitleContaining(search,
            pageable)
        .map(infoSharePost -> {
          User user = this.userRepository.findById(infoSharePost.getUserId())
              .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
          return InfoSharePostDto.fromEntityWithUserInfo(infoSharePost, UserDto.fromEntity(user));
        });
  }

  public Page<InfoSharePostDto> getInfoSharePostsByUserId(Long userId, Integer page,
      String search) {
    Pageable pageable = PageRequest.of(page, 10);
    return this.infoSharePostRepository.findAllByUserIdAndTitleContaining(userId, search, pageable)
        .map(infoSharePost -> {
          User user = this.userRepository.findById(infoSharePost.getUserId())
              .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
          return InfoSharePostDto.fromEntityWithUserInfo(infoSharePost, UserDto.fromEntity(user));
        });
  }

  public InfoSharePostDto getInfoSharePostByPostId(Long infoPostId) {
    InfoSharePost infoSharePost = this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    UserDto userDto = UserDto.fromEntity(this.userRepository.findById(infoSharePost.getUserId())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND)));
    return InfoSharePostDto.fromEntityWithUserInfo(infoSharePost, userDto);
  }

  public InfoSharePostDto updateInfoSharePost(Long infoPostId, InfoSharePostDto infoSharePostDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    if (Long.parseLong(userDetails.getUsername()) != infoSharePostDto.getUserId()) {
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
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    if (Long.parseLong(userDetails.getUsername()) != infoSharePost.getUserId()) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }
    this.infoSharePostRepository.deleteById(infoPostId);
  }
}
