package com.socialapp.repository;

import com.socialapp.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    Optional<MessageReaction> findByMessageIdAndUserId(Long messageId, Long userId);

    List<MessageReaction> findByMessageId(Long messageId);

    void deleteByMessageIdAndUserId(Long messageId, Long userId);
}
