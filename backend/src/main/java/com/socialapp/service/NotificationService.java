package com.socialapp.service;

import com.socialapp.dto.NotificationDto;
import com.socialapp.dto.PageResponse;
import com.socialapp.entity.Notification;
import com.socialapp.entity.User;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Internal - called by PostService/CommentService/FollowService/MessageService
     * when a like/comment/follow/message happens. No-ops if actor == recipient
     * (don't notify yourself for your own actions).
     */
    void notify(User recipient, User actor, Notification.NotificationType type,
                Long postId, Long commentId, Long messageId);

    PageResponse<NotificationDto> getNotifications(String username, Pageable pageable);

    void markAsRead(String username, Long notificationId);

    void markAllAsRead(String username);

    long getUnreadCount(String username);
}
