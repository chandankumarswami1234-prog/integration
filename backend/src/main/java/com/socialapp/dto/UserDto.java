package com.socialapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String profilePictureUrl;
    private String coverPictureUrl;
    private String location;
    private String website;
    private LocalDate dateOfBirth;
    private boolean emailVerified;
    private long followerCount;
    private long followingCount;
    private boolean followedByCurrentUser;
    private LocalDateTime createdAt;
}
