package com.socialapp.controller;

import com.socialapp.dto.*;
import com.socialapp.security.CurrentUserProvider;
import com.socialapp.service.MessageService;
import com.socialapp.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        String sender = currentUserProvider.getCurrentUsername();
        MessageDto message = messageService.sendMessage(sender, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Message sent", message));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<PageResponse<ConversationDto>>> getInbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String viewer = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ConversationDto> inbox = messageService.getInbox(viewer, pageable);
        return ResponseEntity.ok(ApiResponse.success(inbox));
    }

    @GetMapping("/conversation/{username}")
    public ResponseEntity<ApiResponse<PageResponse<MessageDto>>> getConversation(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String viewer = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<MessageDto> conversation = messageService.getConversation(viewer, username, pageable);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @PutMapping("/conversation/{username}/read")
    public ResponseEntity<ApiResponse<Void>> markConversationAsRead(@PathVariable String username) {
        String viewer = currentUserProvider.getCurrentUsername();
        messageService.markConversationAsRead(viewer, username);
        return ResponseEntity.ok(ApiResponse.success("Conversation marked as read", null));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        String viewer = currentUserProvider.getCurrentUsername();
        messageService.deleteMessage(viewer, messageId);
        return ResponseEntity.ok(ApiResponse.success("Message deleted", null));
    }

    @PostMapping("/{messageId}/react")
    public ResponseEntity<ApiResponse<MessageDto>> react(@PathVariable Long messageId,
                                                           @Valid @RequestBody ReactionRequest request) {
        String viewer = currentUserProvider.getCurrentUsername();
        MessageDto message = messageService.reactToMessage(viewer, messageId, request);
        return ResponseEntity.ok(ApiResponse.success("Reaction added", message));
    }

    @DeleteMapping("/{messageId}/react")
    public ResponseEntity<ApiResponse<Void>> removeReaction(@PathVariable Long messageId) {
        String viewer = currentUserProvider.getCurrentUsername();
        messageService.removeReaction(viewer, messageId);
        return ResponseEntity.ok(ApiResponse.success("Reaction removed", null));
    }
}
