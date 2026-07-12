package com.socialapp.service;

import com.socialapp.dto.CommentDto;
import com.socialapp.dto.CreateCommentRequest;
import com.socialapp.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentDto addComment(String username, Long postId, CreateCommentRequest request);

    void deleteComment(String username, Long commentId);

    PageResponse<CommentDto> getTopLevelComments(Long postId, Pageable pageable);

    PageResponse<CommentDto> getReplies(Long commentId, Pageable pageable);
}
