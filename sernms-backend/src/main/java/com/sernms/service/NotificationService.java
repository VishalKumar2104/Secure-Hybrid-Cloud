package com.sernms.service;

import com.sernms.entity.Notification;
import com.sernms.entity.User;
import com.sernms.exception.ResourceNotFoundException;
import com.sernms.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void createGlobalNotification(String title, String message, String type) {
        Notification notification = new Notification(null, title, message, type);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createUserNotification(User recipient, String title, String message, String type) {
        Notification notification = new Notification(recipient, title, message, type);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(Long userId) {
        if (userId == null) {
            return notificationRepository.findByRecipientIsNullOrderByCreatedAtDesc();
        }
        List<Notification> userNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        List<Notification> globalNotifs = notificationRepository.findByRecipientIsNullOrderByCreatedAtDesc();
        userNotifs.addAll(globalNotifs);
        userNotifs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return userNotifs;
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
