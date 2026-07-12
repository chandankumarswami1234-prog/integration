package com.socialapp.controller;

import com.socialapp.dto.PageResponse;
import com.socialapp.dto.UserDto;
import com.socialapp.security.CurrentUserProvider;
import com.socialapp.service.FollowService;
import com.socialapp.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{username}")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<UserDto>> follow(@PathVariable String username) {
        String follower = currentUserProvider.getCurrentUsername();
        UserDto target = followService.follow(follower, username);
        return ResponseEntity.ok(ApiResponse.success("Followed", target));
    }

    @DeleteMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(@PathVariable String username) {
        String follower = currentUserProvider.getCurrentUsername();
        followService.unfollow(follower, username);
        return ResponseEntity.ok(ApiResponse.success("Unfollowed", null));
    }

    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String viewer = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<UserDto> followers = followService.getFollowers(viewer, username, pageable);
        return ResponseEntity.ok(ApiResponse.success(followers));
    }

    @GetMapping("/following")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String viewer = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<UserDto> following = followService.getFollowing(viewer, username, pageable);
        return ResponseEntity.ok(ApiResponse.success(following));
    }
}
