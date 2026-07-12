package com.socialapp.controller;

import com.socialapp.dto.CommentDto;
import com.socialapp.dto.CreateCommentRequest;
import com.socialapp.dto.PageResponse;
import com.socialapp.security.CurrentUserProvider;
import com.socialapp.service.CommentService;
import com.socialapp.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentDto>> addComment(@PathVariable Long postId,
                                                                @Valid @RequestBody CreateCommentRequest request) {
        String username = currentUserProvider.getCurrentUsername();
        CommentDto comment = commentService.addComment(username, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Comment added", comment));
    }

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentDto>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<CommentDto> comments = commentService.getTopLevelComments(postId, pageable);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/api/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<PageResponse<CommentDto>>> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<CommentDto> replies = commentService.getReplies(commentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(replies));
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        String username = currentUserProvider.getCurrentUsername();
        commentService.deleteComment(username, commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }
}
