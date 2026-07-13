package com.socialapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_sender", columnList = "sender_id"),
        @Index(name = "idx_messages_recipient", columnList = "recipient_id"),
        @Index(name = "idx_messages_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(columnDefinition = "TEXT")
    private String content;

    // URL to an already-uploaded file (same pattern as Post.mediaUrls) - actual
    // upload to Cloudinary is a separate integration, not built yet.
    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", length = 20)
    private AttachmentType attachmentType;

    @Column(nullable = false)
    @Builder.Default
    private boolean delivered = true; // REST send = server received it; true "delivered" push needs a live connection

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Soft delete, per side - "delete for me" without affecting the other person's copy.
    @Column(name = "deleted_by_sender", nullable = false)
    @Builder.Default
    private boolean deletedBySender = false;

    @Column(name = "deleted_by_recipient", nullable = false)
    @Builder.Default
    private boolean deletedByRecipient = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum AttachmentType {
        IMAGE, VOICE
    }
}
