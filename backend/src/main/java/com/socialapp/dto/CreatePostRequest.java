package com.socialapp.dto;

import com.socialapp.entity.Post;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @Size(max = 5000, message = "Post content must be under 5000 characters")
    private String content;

    private List<String> mediaUrls;

    private List<String> hashtags;

    private Post.PostType postType;

    private boolean draft;

    private LocalDateTime scheduledAt;
}
