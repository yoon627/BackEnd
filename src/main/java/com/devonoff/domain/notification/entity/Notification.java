package com.devonoff.domain.notification.entity;

import com.devonoff.common.entity.BaseTimeEntity;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.NotificationType;
import com.devonoff.type.PostType;
import jakarta.persistence.*;
import lombok.*;
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
    private String postContent;
    private String commentContent;
    private String replyContent;
    private String studyName;
    private Long targetId;
    private boolean isRead = false;
}
