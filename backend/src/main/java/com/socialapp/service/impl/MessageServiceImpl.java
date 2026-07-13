package com.socialapp.service.impl;

import com.socialapp.dto.ConversationDto;
import com.socialapp.dto.MessageDto;
import com.socialapp.dto.PageResponse;
import com.socialapp.dto.ReactionRequest;
import com.socialapp.dto.SendMessageRequest;
import com.socialapp.entity.Message;
import com.socialapp.entity.MessageReaction;
import com.socialapp.entity.User;
import com.socialapp.exception.ApiException;
import com.socialapp.mapper.MessageMapper;
import com.socialapp.mapper.UserMapper;
import com.socialapp.repository.BlockRepository;
import com.socialapp.repository.MessageReactionRepository;
import com.socialapp.repository.MessageRepository;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageReactionRepository messageReactionRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public MessageDto sendMessage(String senderUsername, SendMessageRequest request) {
        User sender = getUserOrThrow(senderUsername);
        User recipient = getUserOrThrow(request.getRecipientUsername());

        if (sender.getId().equals(recipient.getId())) {
            throw new ApiException("You cannot message yourself", HttpStatus.BAD_REQUEST);
        }
        if (blockRepository.existsBlockBetween(sender.getId(), recipient.getId())) {
            throw new ApiException("You cannot message this user", HttpStatus.FORBIDDEN);
        }
        boolean hasContent = request.getContent() != null && !request.getContent().isBlank();
        boolean hasAttachment = request.getAttachmentUrl() != null && !request.getAttachmentUrl().isBlank();
        if (!hasContent && !hasAttachment) {
            throw new ApiException("Message must have content or an attachment", HttpStatus.BAD_REQUEST);
        }

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentType(request.getAttachmentType())
                .build();

        message = messageRepository.save(message);
        return messageMapper.toDto(message, sender.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageDto> getConversation(String viewerUsername, String otherUsername, Pageable pageable) {
        User viewer = getUserOrThrow(viewerUsername);
        User other = getUserOrThrow(otherUsername);

        Page<Message> page = messageRepository.findConversation(viewer.getId(), other.getId(), pageable);
        Page<MessageDto> dtoPage = page.map(m -> messageMapper.toDto(m, viewer.getId()));
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConversationDto> getInbox(String viewerUsername, Pageable pageable) {
        User viewer = getUserOrThrow(viewerUsername);

        // Conversation partner counts are expected to be small (dozens, not millions)
        // for this project's scale, so we materialize the full list and paginate in
        // Java rather than pushing "latest message per conversation" down into SQL,
        // which would need window functions / native DISTINCT ON and lose portability.
        List<Long> partnerIds = messageRepository.findConversationPartnerIds(viewer.getId());

        List<ConversationDto> conversations = new ArrayList<>();
        for (Long partnerId : partnerIds) {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) {
                continue;
            }
            Page<Message> latest = messageRepository.findConversation(
                    viewer.getId(), partnerId, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")));
            if (latest.isEmpty()) {
                continue;
            }
            long unread = messageRepository.countUnread(viewer.getId(), partnerId);
            conversations.add(ConversationDto.builder()
                    .otherUser(userMapper.toDto(partner))
                    .lastMessage(messageMapper.toDto(latest.getContent().get(0), viewer.getId()))
                    .unreadCount(unread)
                    .build());
        }

        conversations.sort(Comparator.comparing(
                (ConversationDto c) -> c.getLastMessage().getCreatedAt()).reversed());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), conversations.size());
        List<ConversationDto> pageContent = start >= conversations.size()
                ? List.of()
                : conversations.subList(start, end);

        Page<ConversationDto> page = new PageImpl<>(pageContent, pageable, conversations.size());
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public void markConversationAsRead(String viewerUsername, String otherUsername) {
        User viewer = getUserOrThrow(viewerUsername);
        User other = getUserOrThrow(otherUsername);
        messageRepository.markConversationAsRead(viewer.getId(), other.getId(), LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteMessage(String viewerUsername, Long messageId) {
        User viewer = getUserOrThrow(viewerUsername);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException("Message not found", HttpStatus.NOT_FOUND));

        boolean isSender = message.getSender().getId().equals(viewer.getId());
        boolean isRecipient = message.getRecipient().getId().equals(viewer.getId());
        if (!isSender && !isRecipient) {
            throw new ApiException("You do not have permission to delete this message", HttpStatus.FORBIDDEN);
        }

        // Which flag to set depends on the viewer's role on THIS message, not a fixed
        // assumption - the same person can be sender on one message and recipient on
        // the next within the same conversation.
        if (isSender) {
            message.setDeletedBySender(true);
        } else {
            message.setDeletedByRecipient(true);
        }
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public MessageDto reactToMessage(String viewerUsername, Long messageId, ReactionRequest request) {
        User viewer = getUserOrThrow(viewerUsername);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException("Message not found", HttpStatus.NOT_FOUND));

        assertParticipant(viewer.getId(), message);

        MessageReaction reaction = messageReactionRepository.findByMessageIdAndUserId(messageId, viewer.getId())
                .orElse(null);

        if (reaction == null) {
            reaction = MessageReaction.builder()
                    .message(message)
                    .user(viewer)
                    .emoji(request.getEmoji())
                    .build();
        } else {
            reaction.setEmoji(request.getEmoji());
        }
        messageReactionRepository.save(reaction);

        return messageMapper.toDto(message, viewer.getId());
    }

    @Override
    @Transactional
    public void removeReaction(String viewerUsername, Long messageId) {
        User viewer = getUserOrThrow(viewerUsername);
        messageReactionRepository.deleteByMessageIdAndUserId(messageId, viewer.getId());
    }

    // ---- helpers ----

    private void assertParticipant(Long viewerId, Message message) {
        boolean isSender = message.getSender().getId().equals(viewerId);
        boolean isRecipient = message.getRecipient().getId().equals(viewerId);
        if (!isSender && !isRecipient) {
            throw new ApiException("You do not have permission to react to this message", HttpStatus.FORBIDDEN);
        }
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
