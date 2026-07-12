package com.socialapp.mapper;

import com.socialapp.dto.PostDto;
import com.socialapp.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final UserMapper userMapper;

    public PostDto toDto(Post post, boolean likedByCurrentUser) {
        if (post == null) {
            return null;
        }
        return PostDto.builder()
                .id(post.getId())
                .author(userMapper.toDto(post.getAuthor()))
                .content(post.getContent())
                // new ArrayList<>(...) forces Hibernate to actually read the lazy collection
                // NOW, while the transaction/session is still open - not just hold a reference
                // to an unloaded proxy that would blow up later during JSON serialization.
                .mediaUrls(new ArrayList<>(post.getMediaUrls()))
                .hashtags(new ArrayList<>(post.getHashtags()))
                .postType(post.getPostType())
                .draft(post.isDraft())
                .scheduledAt(post.getScheduledAt())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByCurrentUser(likedByCurrentUser)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
