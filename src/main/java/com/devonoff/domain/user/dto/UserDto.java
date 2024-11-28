package com.devonoff.domain.user.dto;

import com.devonoff.domain.user.entity.User;
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
  private String nickName;
  private String email;
  private String profileImageUrl;

  public static UserDto fromEntity(User user) {
    return UserDto.builder()
        .id(user.getId())
        .nickName(user.getNickName())
        .email(user.getEmail())
        .profileImageUrl(user.getProfileImage())
        .build();
  }

}
