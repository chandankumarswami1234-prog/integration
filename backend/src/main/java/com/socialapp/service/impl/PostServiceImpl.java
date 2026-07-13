package com.socialapp.service.impl;

import com.socialapp.dto.CreatePostRequest;
import com.socialapp.dto.PageResponse;
import com.socialapp.dto.PostDto;
import com.socialapp.dto.UpdatePostRequest;
import com.socialapp.entity.Post;
import com.socialapp.entity.User;
import com.socialapp.exception.ApiException;
import com.socialapp.mapper.PostMapper;
import com.socialapp.repository.BlockRepository;
import com.socialapp.repository.FollowRepository;
import com.socialapp.repository.LikeRepository;
import com.socialapp.repository.MuteRepository;
import com.socialapp.repository.PostRepository;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.NotificationService;
import com.socialapp.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final MuteRepository muteRepository;
    private final NotificationService notificationService;
    private final PostMapper postMapper;

    @Override
    @Transactional
    public PostDto createPost(String username, CreatePostRequest request) {
        User author = getUserOrThrow(username);

        Post post = Post.builder()
                .author(author)
                .content(request.getContent())
                .mediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : List.of())
                .hashtags(request.getHashtags() != null ? request.getHashtags() : List.of())
                .postType(request.getPostType() != null ? request.getPostType() : Post.PostType.TEXT)
                .draft(request.isDraft())
                .scheduledAt(request.getScheduledAt())
                .build();

        post = postRepository.save(post);
        return postMapper.toDto(post, false);
    }

    @Override
    @Transactional
    public PostDto updatePost(String username, Long postId, UpdatePostRequest request) {
        Post post = getPostOrThrow(postId);
        assertOwnerOrAdmin(username, post);

        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getMediaUrls() != null) {
            post.setMediaUrls(request.getMediaUrls());
        }
        if (request.getHashtags() != null) {
            post.setHashtags(request.getHashtags());
        }
        if (request.getDraft() != null) {
            post.setDraft(request.getDraft());
        }

        post = postRepository.save(post);
        boolean liked = likeRepository.existsByPostIdAndUserId(post.getId(), getUserOrThrow(username).getId());
        return postMapper.toDto(post, liked);
    }

    @Override
    @Transactional
    public void deletePost(String username, Long postId) {
        Post post = getPostOrThrow(postId);
        assertOwnerOrAdmin(username, post);
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDto getPost(String username, Long postId) {
        Post post = getPostOrThrow(postId);
        assertNotBlockedRelationship(username, post.getAuthor().getId());
        boolean liked = isLikedByViewer(username, postId);
        return postMapper.toDto(post, liked);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostDto> getFeed(String username, String sort, Pageable pageable) {
        Page<Post> page;
        LocalDateTime now = LocalDateTime.now();
        List<Long> excludedIds = computeExcludedAuthorIds(username);

        if ("trending".equalsIgnoreCase(sort)) {
            LocalDateTime since = now.minusDays(7);
            page = postRepository.findTrending(since, now, excludedIds, pageable);
        } else {
            // default: latest
            page = postRepository.findPublishedFeed(now, excludedIds, pageable);
        }

        return mapPageWithLikeStatus(page, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostDto> getFollowingFeed(String username, Pageable pageable) {
        User user = getUserOrThrow(username);
        List<Long> followingIds = followRepository.findFollowingIds(user.getId());

        if (followingIds.isEmpty()) {
            return PageResponse.<PostDto>builder()
                    .content(List.of())
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
        }

        List<Long> excludedIds = computeExcludedAuthorIds(username);
        Page<Post> page = postRepository.findFollowingFeed(followingIds, LocalDateTime.now(), excludedIds, pageable);
        return mapPageWithLikeStatus(page, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostDto> getUserPosts(String viewerUsername, String targetUsername, Pageable pageable) {
        User target = getUserOrThrow(targetUsername);
        assertNotBlockedRelationship(viewerUsername, target.getId());
        Page<Post> page = postRepository.findByAuthorUsernameAndDraftFalse(targetUsername, pageable);
        return mapPageWithLikeStatus(page, viewerUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostDto> getMyDrafts(String username, Pageable pageable) {
        User user = getUserOrThrow(username);
        Page<Post> page = postRepository.findByAuthorIdAndDraftTrue(user.getId(), pageable);
        return mapPageWithLikeStatus(page, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostDto> searchPosts(String username, String keyword, Pageable pageable) {
        List<Long> excludedIds = computeExcludedAuthorIds(username);
        Page<Post> page = postRepository.searchByContent(keyword, LocalDateTime.now(), excludedIds, pageable);
        return mapPageWithLikeStatus(page, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostDto> getByHashtag(String username, String hashtag, Pageable pageable) {
        String normalized = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
        List<Long> excludedIds = computeExcludedAuthorIds(username);
        Page<Post> page = postRepository.findByHashtag(normalized, LocalDateTime.now(), excludedIds, pageable);
        return mapPageWithLikeStatus(page, username);
    }

    @Override
    @Transactional
    public PostDto likePost(String username, Long postId) {
        Post post = getPostOrThrow(postId);
        User user = getUserOrThrow(username);

        long displayLikeCount = post.getLikeCount();

        if (!likeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            com.socialapp.entity.Like like = com.socialapp.entity.Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            postRepository.incrementLikeCount(postId);
            // Don't mutate post.likeCount here: the bulk UPDATE above is the source of truth.
            // Setting it on the managed entity would also trigger a full-row dirty-check flush,
            // and risks overwriting the DB's real value with a stale in-memory one under concurrent likes.
            displayLikeCount = displayLikeCount + 1;
            notificationService.notify(post.getAuthor(), user, com.socialapp.entity.Notification.NotificationType.LIKE,
                    post.getId(), null, null);
        }

        PostDto dto = postMapper.toDto(post, true);
        dto.setLikeCount(displayLikeCount);
        return dto;
    }

    @Override
    @Transactional
    public void unlikePost(String username, Long postId) {
        Post post = getPostOrThrow(postId);
        User user = getUserOrThrow(username);

        if (likeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            likeRepository.deleteByPostIdAndUserId(postId, user.getId());
            postRepository.decrementLikeCount(postId);
        }
    }

    // ---- helpers ----

    // Combines "blocked either direction" + "muted by viewer" into one exclusion set
    // for feed/search/hashtag queries. Never returns an empty list: JPQL's "NOT IN ()"
    // is invalid SQL, so an unmatched sentinel ID stands in when there's nothing to exclude.
    private List<Long> computeExcludedAuthorIds(String username) {
        if (username == null) {
            return List.of(-1L);
        }
        User user = getUserOrThrow(username);
        List<Long> excluded = new ArrayList<>();
        excluded.addAll(blockRepository.findBlockedIds(user.getId()));
        excluded.addAll(blockRepository.findBlockedByIds(user.getId()));
        excluded.addAll(muteRepository.findMutedIds(user.getId()));
        return excluded.isEmpty() ? List.of(-1L) : excluded;
    }

    private void assertNotBlockedRelationship(String viewerUsername, Long authorId) {
        if (viewerUsername == null) {
            return;
        }
        User viewer = getUserOrThrow(viewerUsername);
        if (blockRepository.existsBlockBetween(viewer.getId(), authorId)) {
            throw new ApiException("Post not found", HttpStatus.NOT_FOUND);
        }
    }

    private PageResponse<PostDto> mapPageWithLikeStatus(Page<Post> page, String viewerUsername) {
        Long viewerId = viewerUsername != null ? getUserOrThrow(viewerUsername).getId() : null;

        Page<PostDto> dtoPage = page.map(post -> {
            boolean liked = viewerId != null && likeRepository.existsByPostIdAndUserId(post.getId(), viewerId);
            return postMapper.toDto(post, liked);
        });

        return PageResponse.from(dtoPage);
    }

    private boolean isLikedByViewer(String username, Long postId) {
        if (username == null) {
            return false;
        }
        User user = getUserOrThrow(username);
        return likeRepository.existsByPostIdAndUserId(postId, user.getId());
    }

    private void assertOwnerOrAdmin(String username, Post post) {
        User requester = getUserOrThrow(username);
        boolean isOwner = post.getAuthor().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ApiException("You do not have permission to modify this post", HttpStatus.FORBIDDEN);
        }
    }

    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException("Post not found", HttpStatus.NOT_FOUND));
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
