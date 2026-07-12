package com.socialapp.controller;

import com.socialapp.dto.PageResponse;
import com.socialapp.dto.UserDto;
import com.socialapp.security.CurrentUserProvider;
import com.socialapp.service.UserService;
import com.socialapp.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(@PathVariable String username) {
        String viewer = currentUserProvider.getCurrentUsername();
        UserDto profile = userService.getProfile(viewer, username);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String viewer = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<UserDto> results = userService.searchUsers(viewer, q, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
