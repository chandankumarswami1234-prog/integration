package com.socialapp.mapper;

import com.socialapp.dto.UserDto;
import com.socialapp.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    private static final int ONLINE_THRESHOLD_MINUTES = 5;

    public UserDto toDto(User user) {
        return toDto(user, false);
    }

    public UserDto toDto(User user, boolean followedByCurrentUser) {
        if (user == null) {
            return null;
        }
        boolean online = user.getLastActiveAt() != null
                && user.getLastActiveAt().isAfter(LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES));

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .coverPictureUrl(user.getCoverPictureUrl())
                .location(user.getLocation())
                .website(user.getWebsite())
                .dateOfBirth(user.getDateOfBirth())
                .emailVerified(user.isEmailVerified())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .followedByCurrentUser(followedByCurrentUser)
                .online(online)
                .lastActiveAt(user.getLastActiveAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
