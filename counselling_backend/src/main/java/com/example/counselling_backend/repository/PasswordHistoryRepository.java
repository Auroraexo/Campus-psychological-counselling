package com.example.counselling_backend.repository;

import com.example.counselling_backend.model.PasswordHistory;
import com.example.counselling_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user = :user ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findRecentPasswordsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(ph) FROM PasswordHistory ph WHERE ph.user = :user AND ph.hashedPassword = :hashedPassword")
    int countByUserAndHashedPassword(@Param("user") User user, @Param("hashedPassword") String hashedPassword);
    
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user = :user ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findTop5ByUserOrderByCreatedAtDesc(@Param("user") User user);
}