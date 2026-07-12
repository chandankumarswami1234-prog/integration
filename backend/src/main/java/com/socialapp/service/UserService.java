package com.socialapp.service;

import com.socialapp.dto.PageResponse;
import com.socialapp.dto.UserDto;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDto getProfile(String viewerUsername, String targetUsername);

    PageResponse<UserDto> searchUsers(String viewerUsername, String keyword, Pageable pageable);
}
