package com.devonoff.domain.notification.dto;

import com.devonoff.domain.notification.entity.Notification;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.type.NotificationType;
import com.devonoff.type.PostType;
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
public class NotificationDto {

  private Long id;
  private Long userId;
  private UserDto sender;
  private NotificationType type;
  private PostType postType;
  private String postTitle;
  private String postContent;
  private String commentContent;
  private String replyContent;
  private String studyName;
  private Long targetId;
  private boolean isRead;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static Notification toEntity(NotificationDto notificationDto) {
    return Notification.builder()
        .userId(notificationDto.getUserId())
        .sender(UserDto.toEntity(notificationDto.getSender()))
        .type(notificationDto.getType())
        .postType(notificationDto.getPostType())
        .postTitle(notificationDto.getPostTitle())
        .postContent(notificationDto.getPostContent())
        .commentContent(notificationDto.getCommentContent())
        .replyContent(notificationDto.getReplyContent())
        .studyName(notificationDto.getStudyName())
        .targetId(notificationDto.getTargetId())
        .isRead(notificationDto.isRead())
        .build();
  }

  public static NotificationDto toDto(Notification notification) {
    return NotificationDto.builder()
        .id(notification.getId())
        .userId(notification.getUserId())
        .sender(UserDto.fromEntity(notification.getSender()))
        .type(notification.getType())
        .postType(notification.getPostType())
        .postTitle(notification.getPostTitle())
        .postContent(notification.getPostContent())
        .commentContent(notification.getCommentContent())
        .replyContent(notification.getReplyContent())
        .studyName(notification.getStudyName())
        .targetId(notification.getTargetId())
        .isRead(notification.isRead())
        .createdAt(notification.getCreatedAt())
        .updatedAt(notification.getUpdatedAt())
        .build();
  }

}
