package com.socialapp.service.impl;

import com.socialapp.dto.PageResponse;
import com.socialapp.dto.UserDto;
import com.socialapp.entity.User;
import com.socialapp.exception.ApiException;
import com.socialapp.mapper.UserMapper;
import com.socialapp.repository.BlockRepository;
import com.socialapp.repository.FollowRepository;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDto getProfile(String viewerUsername, String targetUsername) {
        User target = getUserOrThrow(targetUsername);

        if (viewerUsername != null) {
            User viewer = getUserOrThrow(viewerUsername);
            // Blocked profiles return 404, not 403 - consistent with not confirming
            // block status to the blocked party (same reasoning as elsewhere).
            if (blockRepository.existsBlockBetween(viewer.getId(), target.getId())) {
                throw new ApiException("User not found", HttpStatus.NOT_FOUND);
            }
            boolean followed = followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), target.getId());
            return userMapper.toDto(target, followed);
        }

        return userMapper.toDto(target, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> searchUsers(String viewerUsername, String keyword, Pageable pageable) {
        Page<User> page = userRepository.searchByUsernameOrFullName(keyword, pageable);
        Long viewerId = viewerUsername != null ? getUserOrThrow(viewerUsername).getId() : null;

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
