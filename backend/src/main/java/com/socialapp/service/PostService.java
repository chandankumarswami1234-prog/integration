package com.socialapp.service;

import com.socialapp.dto.CreatePostRequest;
import com.socialapp.dto.PageResponse;
import com.socialapp.dto.PostDto;
import com.socialapp.dto.UpdatePostRequest;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostDto createPost(String username, CreatePostRequest request);

    PostDto updatePost(String username, Long postId, UpdatePostRequest request);

    void deletePost(String username, Long postId);

    PostDto getPost(String username, Long postId);

    PageResponse<PostDto> getFeed(String username, String sort, Pageable pageable);

    PageResponse<PostDto> getFollowingFeed(String username, Pageable pageable);

    PageResponse<PostDto> getUserPosts(String viewerUsername, String targetUsername, Pageable pageable);

    PageResponse<PostDto> getMyDrafts(String username, Pageable pageable);

    PageResponse<PostDto> searchPosts(String username, String keyword, Pageable pageable);

    PageResponse<PostDto> getByHashtag(String username, String hashtag, Pageable pageable);

    PostDto likePost(String username, Long postId);

    void unlikePost(String username, Long postId);
}
