package com.socialapp.dto;

import com.socialapp.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private UserDto actor;
    private Notification.NotificationType type;
    private Long postId;
    private Long commentId;
    private Long messageId;
    private boolean read;
    private LocalDateTime createdAt;
}
