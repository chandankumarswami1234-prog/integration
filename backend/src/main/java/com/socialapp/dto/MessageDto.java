package com.socialapp.dto;

import com.socialapp.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private UserDto sender;
    private UserDto recipient;
    private String content;
    private String attachmentUrl;
    private Message.AttachmentType attachmentType;
    private boolean delivered;
    private boolean read;
    private LocalDateTime readAt;
    private Map<String, Long> reactionCounts;
    private String currentUserReaction;
    private LocalDateTime createdAt;
}
