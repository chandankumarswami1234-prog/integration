package com.socialapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mutes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mute_muter_muted", columnNames = {"muter_id", "muted_id"})
}, indexes = {
        @Index(name = "idx_mutes_muter", columnList = "muter_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-directional and asymmetric: only hides the muted user's posts from
    // the muter's own feed. Unlike Block, the muted user is not notified and
    // can still see/interact with the muter normally.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muter_id", nullable = false)
    private User muter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muted_id", nullable = false)
    private User muted;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
