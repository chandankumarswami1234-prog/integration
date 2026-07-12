package com.socialapp.controller;

import com.socialapp.security.CurrentUserProvider;
import com.socialapp.service.BlockService;
import com.socialapp.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{username}/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> block(@PathVariable String username) {
        String blocker = currentUserProvider.getCurrentUsername();
        blockService.block(blocker, username);
        return ResponseEntity.ok(ApiResponse.success("User blocked", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unblock(@PathVariable String username) {
        String blocker = currentUserProvider.getCurrentUsername();
        blockService.unblock(blocker, username);
        return ResponseEntity.ok(ApiResponse.success("User unblocked", null));
    }
}
