package com.sernms.repository;

import com.sernms.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(Long recipientId);
    List<Notification> findByRecipientIsNullOrderByCreatedAtDesc(); // Global broadcast notifications
    long countByRecipientIdAndReadFalse(Long recipientId);
}
