package com.socialapp.service.impl;

import com.socialapp.dto.PageResponse;
import com.socialapp.dto.UserDto;
import com.socialapp.entity.Follow;
import com.socialapp.entity.User;
import com.socialapp.exception.ApiException;
import com.socialapp.mapper.UserMapper;
import com.socialapp.repository.BlockRepository;
import com.socialapp.repository.FollowRepository;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.FollowService;
import com.socialapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto follow(String followerUsername, String targetUsername) {
        User follower = getUserOrThrow(followerUsername);
        User target = getUserOrThrow(targetUsername);

        if (follower.getId().equals(target.getId())) {
            throw new ApiException("You cannot follow yourself", HttpStatus.BAD_REQUEST);
        }
        if (blockRepository.existsBlockBetween(follower.getId(), target.getId())) {
            // Same "not found" treatment as elsewhere: don't reveal block status.
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }

        long displayFollowerCount = target.getFollowerCount();

        if (!followRepository.existsByFollowerIdAndFollowingId(follower.getId(), target.getId())) {
            Follow follow = Follow.builder().follower(follower).following(target).build();
            followRepository.save(follow);
            userRepository.incrementFollowingCount(follower.getId());
            userRepository.incrementFollowerCount(target.getId());
            // Don't re-fetch target here to "see" the incremented count: within the same
            // transaction, Hibernate's session cache returns the SAME already-loaded object
            // for a repeat query by ID, ignoring the fresh row - the exact staleness bug
            // we hit (and fixed) in PostServiceImpl.likePost. Compute in Java instead.
            displayFollowerCount = displayFollowerCount + 1;
            notificationService.notify(target, follower, com.socialapp.entity.Notification.NotificationType.FOLLOW,
                    null, null, null);
        }

        UserDto dto = userMapper.toDto(target, true);
        dto.setFollowerCount(displayFollowerCount);
        return dto;
    }

    @Override
    @Transactional
    public void unfollow(String followerUsername, String targetUsername) {
        User follower = getUserOrThrow(followerUsername);
        User target = getUserOrThrow(targetUsername);

        if (followRepository.existsByFollowerIdAndFollowingId(follower.getId(), target.getId())) {
            followRepository.deleteByFollowerIdAndFollowingId(follower.getId(), target.getId());
            userRepository.decrementFollowingCount(follower.getId());
            userRepository.decrementFollowerCount(target.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> getFollowers(String viewerUsername, String targetUsername, Pageable pageable) {
        User target = getUserOrThrow(targetUsername);
        Long viewerId = viewerUsername != null ? getUserOrThrow(viewerUsername).getId() : null;

        Page<User> page = followRepository.findFollowers(target.getId(), pageable);
        return mapWithFollowStatus(page, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> getFollowing(String viewerUsername, String targetUsername, Pageable pageable) {
        User target = getUserOrThrow(targetUsername);
        Long viewerId = viewerUsername != null ? getUserOrThrow(viewerUsername).getId() : null;

        Page<User> page = followRepository.findFollowing(target.getId(), pageable);
        return mapWithFollowStatus(page, viewerId);
    }

    private PageResponse<UserDto> mapWithFollowStatus(Page<User> page, Long viewerId) {
        Page<UserDto> dtoPage = page.map(user -> {
            boolean followed = viewerId != null
                    && followRepository.existsByFollowerIdAndFollowingId(viewerId, user.getId());
            return userMapper.toDto(user, followed);
        });
        return PageResponse.from(dtoPage);
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
