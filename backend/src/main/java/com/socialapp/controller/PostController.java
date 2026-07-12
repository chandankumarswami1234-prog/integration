package com.socialapp.controller;

import com.socialapp.dto.CreatePostRequest;
import com.socialapp.dto.PageResponse;
import com.socialapp.dto.PostDto;
import com.socialapp.dto.UpdatePostRequest;
import com.socialapp.security.CurrentUserProvider;
import com.socialapp.service.PostService;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDto>> createPost(@Valid @RequestBody CreatePostRequest request) {
        String username = currentUserProvider.getCurrentUsername();
        PostDto post = postService.createPost(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Post created", post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> updatePost(@PathVariable Long id,
                                                             @Valid @RequestBody UpdatePostRequest request) {
        String username = currentUserProvider.getCurrentUsername();
        PostDto post = postService.updatePost(username, id, request);
        return ResponseEntity.ok(ApiResponse.success("Post updated", post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        String username = currentUserProvider.getCurrentUsername();
        postService.deletePost(username, id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> getPost(@PathVariable Long id) {
        String username = currentUserProvider.getCurrentUsername();
        PostDto post = postService.getPost(username, id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    /**
     * Feed. sort=latest (default) or sort=trending (last 7 days, most liked first).
     * Pagination: ?page=0&size=20
     */
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PageResponse<PostDto>>> getFeed(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String username = currentUserProvider.getCurrentUsername();
        Sort sortOrder = "trending".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "likeCount")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        PageResponse<PostDto> feed = postService.getFeed(username, sort, pageable);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    /**
     * Feed of only the posts from users the caller follows. Requires authentication
     * (there's no anonymous "following" - the caller's identity IS the filter).
     */
    @GetMapping("/feed/following")
    public ResponseEntity<ApiResponse<PageResponse<PostDto>>> getFollowingFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String username = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PostDto> feed = postService.getFollowingFeed(username, pageable);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<PageResponse<PostDto>>> getUserPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String viewer = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PostDto> posts = postService.getUserPosts(viewer, username, pageable);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/drafts")
    public ResponseEntity<ApiResponse<PageResponse<PostDto>>> getMyDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String username = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PostDto> drafts = postService.getMyDrafts(username, pageable);
        return ResponseEntity.ok(ApiResponse.success(drafts));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostDto>>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String username = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PostDto> results = postService.searchPosts(username, q, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/hashtag/{tag}")
    public ResponseEntity<ApiResponse<PageResponse<PostDto>>> getByHashtag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String username = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PostDto> results = postService.getByHashtag(username, tag, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<PostDto>> likePost(@PathVariable Long id) {
        String username = currentUserProvider.getCurrentUsername();
        PostDto post = postService.likePost(username, id);
        return ResponseEntity.ok(ApiResponse.success("Post liked", post));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> unlikePost(@PathVariable Long id) {
        String username = currentUserProvider.getCurrentUsername();
        postService.unlikePost(username, id);
        return ResponseEntity.ok(ApiResponse.success("Post unliked", null));
    }
}
