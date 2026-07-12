package com.socialapp.service.impl;

import com.socialapp.entity.Block;
import com.socialapp.entity.User;
import com.socialapp.exception.ApiException;
import com.socialapp.repository.BlockRepository;
import com.socialapp.repository.FollowRepository;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void block(String blockerUsername, String targetUsername) {
        User blocker = getUserOrThrow(blockerUsername);
        User target = getUserOrThrow(targetUsername);

        if (blocker.getId().equals(target.getId())) {
            throw new ApiException("You cannot block yourself", HttpStatus.BAD_REQUEST);
        }

        if (!blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), target.getId())) {
            Block block = Block.builder().blocker(blocker).blocked(target).build();
            blockRepository.save(block);

            // Blocking severs any existing follow relationship in either direction,
            // with counters kept in sync.
            removeFollowIfExists(blocker.getId(), target.getId());
            removeFollowIfExists(target.getId(), blocker.getId());
        }
    }

    @Override
    @Transactional
    public void unblock(String blockerUsername, String targetUsername) {
        User blocker = getUserOrThrow(blockerUsername);
        User target = getUserOrThrow(targetUsername);
        blockRepository.deleteByBlockerIdAndBlockedId(blocker.getId(), target.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlocked(String username1, String username2) {
        User user1 = getUserOrThrow(username1);
        User user2 = getUserOrThrow(username2);
        return blockRepository.existsBlockBetween(user1.getId(), user2.getId());
    }

    private void removeFollowIfExists(Long followerId, Long followingId) {
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
            userRepository.decrementFollowingCount(followerId);
            userRepository.decrementFollowerCount(followingId);
        }
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
