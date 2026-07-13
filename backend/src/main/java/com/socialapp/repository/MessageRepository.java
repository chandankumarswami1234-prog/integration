package com.socialapp.repository;

import com.socialapp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Every query below is written from the VIEWER's perspective: a message is
    // visible to the viewer unless THEY specifically soft-deleted it - checking
    // deletedBySender when the viewer was the sender on that message, or
    // deletedByRecipient when the viewer was the recipient. Since sender/recipient
    // roles alternate message-by-message within one conversation, this can't be
    // simplified to a single flag - both branches are required in every query here.

    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :viewerId AND m.recipient.id = :otherId AND m.deletedBySender = false) " +
           "OR (m.sender.id = :otherId AND m.recipient.id = :viewerId AND m.deletedByRecipient = false)")
    Page<Message> findConversation(@Param("viewerId") Long viewerId,
                                    @Param("otherId") Long otherId,
                                    Pageable pageable);

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :viewerId THEN m.recipient.id ELSE m.sender.id END " +
           "FROM Message m WHERE " +
           "(m.sender.id = :viewerId AND m.deletedBySender = false) " +
           "OR (m.recipient.id = :viewerId AND m.deletedByRecipient = false)")
    List<Long> findConversationPartnerIds(@Param("viewerId") Long viewerId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.id = :otherId AND m.recipient.id = :viewerId " +
           "AND m.read = false AND m.deletedByRecipient = false")
    long countUnread(@Param("viewerId") Long viewerId, @Param("otherId") Long otherId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.read = true, m.readAt = :now " +
           "WHERE m.sender.id = :otherId AND m.recipient.id = :viewerId AND m.read = false")
    void markConversationAsRead(@Param("viewerId") Long viewerId,
                                 @Param("otherId") Long otherId,
                                 @Param("now") LocalDateTime now);
}
