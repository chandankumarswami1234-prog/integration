package com.socialapp.controller;

import com.socialapp.security.CurrentUserProvider;
import com.socialapp.service.MuteService;
import com.socialapp.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{username}/mute")
@RequiredArgsConstructor
public class MuteController {

    private final MuteService muteService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> mute(@PathVariable String username) {
        String muter = currentUserProvider.getCurrentUsername();
        muteService.mute(muter, username);
        return ResponseEntity.ok(ApiResponse.success("User muted", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unmute(@PathVariable String username) {
        String muter = currentUserProvider.getCurrentUsername();
        muteService.unmute(muter, username);
        return ResponseEntity.ok(ApiResponse.success("User unmuted", null));
    }
}
