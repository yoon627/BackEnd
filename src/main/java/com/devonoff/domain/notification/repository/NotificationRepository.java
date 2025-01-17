package com.devonoff.domain.notification.repository;

import com.devonoff.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findAllByUserId(Long userId);

  void deleteAllByUserId(Long userId);

  Page<Notification> findAllByUserIdAndCreatedAtAfter(Long userId, LocalDateTime fourteenDaysAgo,
      Pageable pageable);
}
