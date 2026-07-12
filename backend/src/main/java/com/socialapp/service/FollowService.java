package com.socialapp.service;

import com.socialapp.dto.PageResponse;
import com.socialapp.dto.UserDto;
import org.springframework.data.domain.Pageable;

public interface FollowService {

    UserDto follow(String followerUsername, String targetUsername);

    void unfollow(String followerUsername, String targetUsername);

    PageResponse<UserDto> getFollowers(String viewerUsername, String targetUsername, Pageable pageable);

    PageResponse<UserDto> getFollowing(String viewerUsername, String targetUsername, Pageable pageable);
}
