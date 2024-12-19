package com.devonoff.domain.user.dto;

import com.devonoff.domain.user.entity.User;
import com.devonoff.type.LoginType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

  private Long id;
  private String nickname;
  private String email;
  private String profileImageUrl;
  private Boolean isActive;
  private LoginType signinType;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static UserDto fromEntity(User user) {
    return UserDto.builder()
        .id(user.getId())
        .nickname(user.getNickname())
        .email(user.getEmail())
        .profileImageUrl(user.getProfileImage())
        .isActive(user.getIsActive())
        .signinType(user.getLoginType())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  public static User toEntity(UserDto userDto) {
    return User.builder()
        .id(userDto.getId())
        .nickname(userDto.getNickname())
        .email(userDto.getEmail())
        .profileImage(userDto.getProfileImageUrl())
        .build();
  }
}
