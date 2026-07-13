package com.socialapp.mapper;

import com.socialapp.dto.NotificationDto;
import com.socialapp.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final UserMapper userMapper;

    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationDto.builder()
                .id(notification.getId())
                .actor(userMapper.toDto(notification.getActor()))
                .type(notification.getType())
                .postId(notification.getPostId())
                .commentId(notification.getCommentId())
                .messageId(notification.getMessageId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
