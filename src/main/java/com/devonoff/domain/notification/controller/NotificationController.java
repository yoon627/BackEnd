package com.devonoff.domain.notification.controller;

import com.devonoff.domain.notification.dto.NotificationDto;
import com.devonoff.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<Page<NotificationDto>> getNotifications(@RequestParam int page) {
    Page<NotificationDto> result = notificationService.getNotificationsByUserId(page);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/{notificationId}")
  public ResponseEntity<NotificationDto> readNotification(@PathVariable Long notificationId) {
    NotificationDto result = notificationService.readNotification(notificationId);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{notificationId}")
  public void deleteNotification(@PathVariable Long notificationId) {
    notificationService.deleteNotification(notificationId);
  }

  @PatchMapping("/user/{userId}")
  public void readNotifications(@PathVariable Long userId) {
    notificationService.readNotifications(userId);
  }

  @DeleteMapping("/user/{userId}")
  public void deleteNotifications(@PathVariable Long userId) {
    notificationService.deleteNotifications(userId);
  }
}
