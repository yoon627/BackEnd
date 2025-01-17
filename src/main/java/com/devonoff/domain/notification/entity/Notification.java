package com.devonoff.domain.notification.entity;

import com.devonoff.common.entity.BaseTimeEntity;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.NotificationType;
import com.devonoff.type.PostType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long userId;
  @ManyToOne
  @JoinColumn(name = "sender_id")
  private User sender;
  private NotificationType type;
  private PostType postType;
  private String postTitle;
  @Column(columnDefinition = "TEXT")
  private String postContent;
  @Column(columnDefinition = "TEXT")
  private String commentContent;
  @Column(columnDefinition = "TEXT")
  private String replyContent;
  private String studyName;
  private Long targetId;
  private boolean isRead = false;
}
