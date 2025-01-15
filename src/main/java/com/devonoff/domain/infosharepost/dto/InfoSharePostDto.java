package com.devonoff.domain.infosharepost.dto;

import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.user.dto.UserDto;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfoSharePostDto {

  private Long id;
  private UserDto user;
  private String thumbnailImgUrl;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long userId;
  @Nullable
  private MultipartFile file;

  public static InfoSharePostDto fromEntity(InfoSharePost post) {
    return InfoSharePostDto.builder()
        .id(post.getId())
        .user(UserDto.fromEntity(post.getUser()))
        .thumbnailImgUrl(post.getThumbnailImgUrl())
        .title(post.getTitle())
        .content(post.getContent())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .userId(post.getUser().getId())
        .build();
  }

  public static InfoSharePost toEntity(InfoSharePostDto infoSharePostDto) {
    return InfoSharePost.builder()
        .user(UserDto.toEntity(infoSharePostDto.getUser()))
        .thumbnailImgUrl(infoSharePostDto.getThumbnailImgUrl())
        .title(infoSharePostDto.getTitle())
        .content(infoSharePostDto.getContent())
        .build();
  }

}