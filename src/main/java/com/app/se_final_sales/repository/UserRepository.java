package com.app.se_final_sales.repository;

import com.sefinal.erp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.userId = :id")
    void incrementFailedAttempts(@Param("id") Long userId);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.lockedUntil = :until WHERE u.userId = :id")
    void lockUntil(@Param("id") Long userId, @Param("until") LocalDateTime until);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.userId = :id")
    void clearLoginCounters(@Param("id") Long userId);
}
