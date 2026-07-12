package com.socialapp.service.impl;

import com.socialapp.dto.CommentDto;
import com.socialapp.dto.CreateCommentRequest;
import com.socialapp.dto.PageResponse;
import com.socialapp.entity.Comment;
import com.socialapp.entity.Post;
import com.socialapp.entity.User;
import com.socialapp.exception.ApiException;
import com.socialapp.mapper.CommentMapper;
import com.socialapp.repository.CommentRepository;
import com.socialapp.repository.PostRepository;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto addComment(String username, Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException("Post not found", HttpStatus.NOT_FOUND));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Comment parent = null;
        if (request.getParentCommentId() != null) {
            parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ApiException("Parent comment not found", HttpStatus.NOT_FOUND));
            if (!parent.getPost().getId().equals(postId)) {
                throw new ApiException("Parent comment does not belong to this post", HttpStatus.BAD_REQUEST);
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .parentComment(parent)
                .content(request.getContent())
                .build();

        comment = commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);

        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(String username, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException("Comment not found", HttpStatus.NOT_FOUND));
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        boolean isOwner = comment.getAuthor().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ApiException("You do not have permission to delete this comment", HttpStatus.FORBIDDEN);
        }

        Long postId = comment.getPost().getId();
        commentRepository.delete(comment);
        postRepository.decrementCommentCount(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentDto> getTopLevelComments(Long postId, Pageable pageable) {
        Page<Comment> page = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId, pageable);
        return PageResponse.from(page.map(commentMapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentDto> getReplies(Long commentId, Pageable pageable) {
        Page<Comment> page = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId, pageable);
        return PageResponse.from(page.map(commentMapper::toDto));
    }
}
