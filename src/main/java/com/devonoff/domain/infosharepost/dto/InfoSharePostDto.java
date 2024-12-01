package com.devonoff.domain.infosharepost.dto;

import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.user.dto.UserDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfoSharePostDto {

  private Long id;
  private Long userId;
  private String thumbnailImgUrl;
  private String title;
  private String description;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private UserDto user;

  public static InfoSharePostDto fromEntity(InfoSharePost post) {
    return InfoSharePostDto.builder()
        .id(post.getId())
        .userId(post.getUserId())
        .thumbnailImgUrl(post.getThumbnailImgUrl())
        .title(post.getTitle())
        .description(post.getDescription())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }

  public static InfoSharePost toEntity(InfoSharePostDto infoSharePostDto) {
    return InfoSharePost.builder()
        .userId(infoSharePostDto.getUserId())
        .thumbnailImgUrl(infoSharePostDto.getThumbnailImgUrl())
        .title(infoSharePostDto.getTitle())
        .description(infoSharePostDto.getDescription())
        .build();
  }

  public static InfoSharePostDto fromEntityWithUserInfo(InfoSharePost infoSharePost,
      UserDto userDto) {
    InfoSharePostDto infoSharePostDto = InfoSharePostDto.fromEntity(infoSharePost);
    infoSharePostDto.setUser(userDto);
    return infoSharePostDto;
  }
}
