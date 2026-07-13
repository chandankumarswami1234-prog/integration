package com.socialapp.service;

import com.socialapp.dto.ConversationDto;
import com.socialapp.dto.MessageDto;
import com.socialapp.dto.PageResponse;
import com.socialapp.dto.ReactionRequest;
import com.socialapp.dto.SendMessageRequest;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    MessageDto sendMessage(String senderUsername, SendMessageRequest request);

    PageResponse<MessageDto> getConversation(String viewerUsername, String otherUsername, Pageable pageable);

    PageResponse<ConversationDto> getInbox(String viewerUsername, Pageable pageable);

    void markConversationAsRead(String viewerUsername, String otherUsername);

    void deleteMessage(String viewerUsername, Long messageId);

    MessageDto reactToMessage(String viewerUsername, Long messageId, ReactionRequest request);

    void removeReaction(String viewerUsername, Long messageId);
}
