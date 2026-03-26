package com.example.counselling_backend.repository;

import com.example.counselling_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :password")
    void updateAllPasswords(String password);
    
    @Query("""
        SELECT u FROM User u
        WHERE (:role IS NULL OR u.role = :role)
          AND (:active IS NULL OR u.active = :active)
          AND (
            :q IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(u.realName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """)
    Page<User> searchUsers(@Param("role") User.Role role,
                           @Param("active") Boolean active,
                           @Param("q") String q,
                           Pageable pageable);
}