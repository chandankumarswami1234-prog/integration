package com.socialapp.dto;

import com.socialapp.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;
    private UserDto author;
    private String content;
    private List<String> mediaUrls;
    private List<String> hashtags;
    private Post.PostType postType;
    private boolean draft;
    private LocalDateTime scheduledAt;
    private long likeCount;
    private long commentCount;
    private boolean likedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
