package com.devonoff.domain.reply.dto;

import com.devonoff.domain.reply.entity.Reply;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.type.PostType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ReplyResponse {

  private Long id;
  private boolean isSecret;
  private Long postId;
  private PostType postType;
  private String content;
  private UserDto userDto;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static ReplyResponse fromEntity(Reply reply) {
    return ReplyResponse.builder()
        .id(reply.getId())
        .postType(reply.getPostType())
        .postId(reply.getPostId())
        .isSecret(reply.getIsSecret())
        .content(reply.getContent())
        .userDto(UserDto.fromEntity(reply.getUser()))  // User 엔티티에서 UserDto로 변환
        .createdAt(reply.getCreatedAt())
        .updatedAt(reply.getUpdatedAt())
        .build();
  }

//  {
//    "id": 1,
//      "post_type": "STUDY",
//      "post_id": 1,
//      "is_secret": false,
//      "content": "본문 내용",
//      "user": {
//    "id": 1,
//        "username": "홍길동",
//        "email": "email@test.com",
//        "profile_image_url": "s3 프로필 이미지 경로",
//  },
//    "created_at": 2024-11-21:21:31:40,
//      "updated_at": 2024-11-21:21:31:40
//  }}

}