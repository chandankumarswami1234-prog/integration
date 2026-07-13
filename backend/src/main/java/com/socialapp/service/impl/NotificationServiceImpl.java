package com.socialapp.service.impl;

import com.socialapp.dto.NotificationDto;
import com.socialapp.dto.PageResponse;
import com.socialapp.entity.Notification;
import com.socialapp.entity.User;
import com.socialapp.exception.ApiException;
import com.socialapp.mapper.NotificationMapper;
import com.socialapp.repository.NotificationRepository;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.NotificationService;
import com.socialapp.service.PushNotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final PushNotificationSender pushNotificationSender;

    @Override
    @Transactional
    public void notify(User recipient, User actor, Notification.NotificationType type,
                        Long postId, Long commentId, Long messageId) {
        if (recipient.getId().equals(actor.getId())) {
            return; // never notify yourself for your own actions
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .postId(postId)
                .commentId(commentId)
                .messageId(messageId)
                .build();

        notification = notificationRepository.save(notification);

        // Best-effort: a push-delivery failure must never break the action that
        // triggered it (the like/comment/follow/message itself already succeeded).
        try {
            pushNotificationSender.send(notification);
        } catch (Exception ignored) {
            // Deliberately swallowed - see PushNotificationSender for why.
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> getNotifications(String username, Pageable pageable) {
        User user = getUserOrThrow(username);
        Page<Notification> page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);
        return PageResponse.from(page.map(notificationMapper::toDto));
    }

    @Override
    @Transactional
    public void markAsRead(String username, Long notificationId) {
        User user = getUserOrThrow(username);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException("Notification not found", HttpStatus.NOT_FOUND));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new ApiException("You do not have permission to modify this notification", HttpStatus.FORBIDDEN);
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String username) {
        User user = getUserOrThrow(username);
        notificationRepository.markAllAsRead(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        User user = getUserOrThrow(username);
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
