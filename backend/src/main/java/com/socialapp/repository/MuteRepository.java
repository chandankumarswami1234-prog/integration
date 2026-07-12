package com.socialapp.repository;

import com.socialapp.entity.Mute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MuteRepository extends JpaRepository<Mute, Long> {

    boolean existsByMuterIdAndMutedId(Long muterId, Long mutedId);

    void deleteByMuterIdAndMutedId(Long muterId, Long mutedId);

    @Query("SELECT m.muted.id FROM Mute m WHERE m.muter.id = :userId")
    List<Long> findMutedIds(@Param("userId") Long userId);
}
