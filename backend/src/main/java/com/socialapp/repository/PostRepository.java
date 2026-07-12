package com.socialapp.repository;

import com.socialapp.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :postId")
    void decrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = CASE WHEN p.commentCount > 0 THEN p.commentCount - 1 ELSE 0 END WHERE p.id = :postId")
    void decrementCommentCount(@Param("postId") Long postId);

    // Public feed: published posts only (no drafts, no future-scheduled posts),
    // excluding anyone blocked (either direction) or muted by the viewer.
    // excludedAuthorIds must never be passed empty - JPQL's "NOT IN ()" is invalid SQL;
    // callers pass List.of(-1L) as a harmless sentinel when there's nothing to exclude.
    @Query("SELECT p FROM Post p WHERE p.draft = false " +
           "AND (p.scheduledAt IS NULL OR p.scheduledAt <= :now) " +
           "AND p.author.id NOT IN :excludedAuthorIds")
    Page<Post> findPublishedFeed(@Param("now") LocalDateTime now,
                                  @Param("excludedAuthorIds") List<Long> excludedAuthorIds,
                                  Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.draft = false " +
           "AND (p.scheduledAt IS NULL OR p.scheduledAt <= :now) " +
           "AND p.createdAt >= :since " +
           "AND p.author.id NOT IN :excludedAuthorIds")
    Page<Post> findTrending(@Param("since") LocalDateTime since,
                             @Param("now") LocalDateTime now,
                             @Param("excludedAuthorIds") List<Long> excludedAuthorIds,
                             Pageable pageable);

    // Following feed: only posts from authors the viewer follows.
    @Query("SELECT p FROM Post p WHERE p.draft = false " +
           "AND (p.scheduledAt IS NULL OR p.scheduledAt <= :now) " +
           "AND p.author.id IN :followingIds " +
           "AND p.author.id NOT IN :excludedAuthorIds")
    Page<Post> findFollowingFeed(@Param("followingIds") List<Long> followingIds,
                                  @Param("now") LocalDateTime now,
                                  @Param("excludedAuthorIds") List<Long> excludedAuthorIds,
                                  Pageable pageable);

    Page<Post> findByAuthorUsernameAndDraftFalse(String username, Pageable pageable);

    Page<Post> findByAuthorIdAndDraftTrue(Long authorId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.draft = false " +
           "AND (p.scheduledAt IS NULL OR p.scheduledAt <= :now) " +
           "AND LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND p.author.id NOT IN :excludedAuthorIds")
    Page<Post> searchByContent(@Param("keyword") String keyword,
                                @Param("now") LocalDateTime now,
                                @Param("excludedAuthorIds") List<Long> excludedAuthorIds,
                                Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.draft = false " +
           "AND (p.scheduledAt IS NULL OR p.scheduledAt <= :now) " +
           "AND :hashtag IN (SELECT h FROM p.hashtags h) " +
           "AND p.author.id NOT IN :excludedAuthorIds")
    Page<Post> findByHashtag(@Param("hashtag") String hashtag,
                              @Param("now") LocalDateTime now,
                              @Param("excludedAuthorIds") List<Long> excludedAuthorIds,
                              Pageable pageable);
}
