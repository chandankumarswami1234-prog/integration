package com.socialapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 500)
    private String bio;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "cover_picture_url")
    private String coverPictureUrl;

    @Column(length = 100)
    private String location;

    @Column(length = 255)
    private String website;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "is_deactivated", nullable = false)
    @Builder.Default
    private boolean deactivated = false;

    // Lightweight online-status approximation: updated on each authenticated
    // request (see JwtAuthFilter). "Online" is inferred as "active in the last
    // few minutes" rather than a true real-time presence signal, which would
    // need a persistent connection (WebSocket) - a separate, later piece.
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    // Denormalized counters - same rationale as Post.likeCount/commentCount:
    // avoids COUNT() queries every time a profile is viewed.
    // columnDefinition with a DEFAULT is required here (not just @Builder.Default,
    // which is Java-side only): without a DB-level default, adding a NOT NULL column
    // to a table that already has rows fails outright, since existing rows have no
    // value to satisfy the constraint. This lets Postgres backfill existing rows with 0.
    @Column(name = "follower_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    @Builder.Default
    private long followerCount = 0;

    @Column(name = "following_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    @Builder.Default
    private long followingCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Role {
        USER, ADMIN
    }
}
