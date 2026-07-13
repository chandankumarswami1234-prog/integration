package com.socialapp.controller;

import com.socialapp.dto.NotificationDto;
import com.socialapp.dto.PageResponse;
import com.socialapp.security.CurrentUserProvider;
import com.socialapp.service.NotificationService;
import com.socialapp.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationDto>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String username = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<NotificationDto> notifications = notificationService.getNotifications(username, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        String username = currentUserProvider.getCurrentUsername();
        long count = notificationService.getUnreadCount(username);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        String username = currentUserProvider.getCurrentUsername();
        notificationService.markAsRead(username, id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        String username = currentUserProvider.getCurrentUsername();
        notificationService.markAllAsRead(username);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
