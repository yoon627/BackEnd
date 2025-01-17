package com.devonoff.domain.notification.service;

import com.devonoff.domain.notification.dto.NotificationDto;
import com.devonoff.domain.notification.entity.Notification;
import com.devonoff.domain.notification.repository.NotificationRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final AuthService authService;

  public void sendNotificationToUser(Long userId, NotificationDto notificationDto) {
    if (Objects.equals(userId, notificationDto.getSender().getId())) {
      return;
    }
    Notification notification = notificationRepository.save(
        NotificationDto.toEntity(notificationDto));
    notificationDto.setId(notification.getId());
    notificationDto.setCreatedAt(notification.getCreatedAt());
    messagingTemplate.convertAndSend("/topic/notifications/" + userId, notificationDto);
  }

  public Page<NotificationDto> getNotificationsByUserId(int pageNumber) {
    Long userId = authService.getLoginUserId();
    LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);
    Pageable pageable = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
    return notificationRepository.findAllByUserIdAndCreatedAtAfter(userId, fourteenDaysAgo,
        pageable).map(NotificationDto::toDto);
  }

  public NotificationDto readNotification(Long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    if (notification.isRead()) {
      throw new CustomException(ErrorCode.NOTIFICATION_ALREADY_READ);
    }
    notification.setRead(true);
    notificationRepository.save(notification);
    return NotificationDto.toDto(notification);
  }

  public void deleteNotification(Long notificationId) {
    notificationRepository.deleteById(notificationId);
  }

  public void readNotifications(Long userId) {
    List<Notification> notifications = notificationRepository.findAllByUserId(userId);
    for (Notification notification : notifications) {
      notification.setRead(true);
      notificationRepository.save(notification);
    }
  }

  @Transactional
  public void deleteNotifications(Long userId) {
    notificationRepository.deleteAllByUserId(userId);
  }
}
