package com.devonoff.domain.notification.repository;

import com.devonoff.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUserId(Long userId, Pageable pageable);

    List<Notification> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
