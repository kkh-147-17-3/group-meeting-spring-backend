package com.sideprj.groupmeeting.repository;

import com.sideprj.groupmeeting.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByScheduledAtBeforeAndSentAtNull(LocalDateTime scheduledAt);
}
