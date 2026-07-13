package com.socialapp.mapper;

import com.socialapp.dto.MessageDto;
import com.socialapp.entity.Message;
import com.socialapp.entity.MessageReaction;
import com.socialapp.repository.MessageReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final UserMapper userMapper;
    private final MessageReactionRepository messageReactionRepository;

    public MessageDto toDto(Message message, Long viewerId) {
        if (message == null) {
            return null;
        }

        List<MessageReaction> reactions = messageReactionRepository.findByMessageId(message.getId());

        Map<String, Long> reactionCounts = reactions.stream()
                .collect(Collectors.groupingBy(MessageReaction::getEmoji, Collectors.counting()));

        String currentUserReaction = reactions.stream()
                .filter(r -> r.getUser().getId().equals(viewerId))
                .map(MessageReaction::getEmoji)
                .findFirst()
                .orElse(null);

        return MessageDto.builder()
                .id(message.getId())
                .sender(userMapper.toDto(message.getSender()))
                .recipient(userMapper.toDto(message.getRecipient()))
                .content(message.getContent())
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentType(message.getAttachmentType())
                .delivered(message.isDelivered())
                .read(message.isRead())
                .readAt(message.getReadAt())
                .reactionCounts(reactionCounts)
                .currentUserReaction(currentUserReaction)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
