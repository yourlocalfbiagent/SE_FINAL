package com.sefinal.erp.admin.repository;

import com.sefinal.erp.admin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    List<User> findByCompanyIdOrderByUserId(int companyId);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.isActive = :active WHERE u.userId = :id")
    void setActive(@Param("id") int userId, @Param("active") boolean active);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.passwordHash = :hash, u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.userId = :id")
    void updatePassword(@Param("id") int userId, @Param("hash") String hash);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.userId = :id")
    void incrementFailedAttempts(@Param("id") int userId);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.lockedUntil = :until WHERE u.userId = :id")
    void lockUntil(@Param("id") int userId, @Param("until") LocalDateTime until);

    @Modifying @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.userId = :id")
    void clearLoginCounters(@Param("id") int userId);

    long countByCompanyId(int companyId);

    long countByCompanyIdAndIsActive(int companyId, boolean isActive);

    @Query("SELECT COUNT(u) FROM User u WHERE u.companyId = :cid AND u.lockedUntil > CURRENT_TIMESTAMP")
    long countLocked(@Param("cid") int companyId);
}
