package com.dataelf.platform.repository;

import com.dataelf.platform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByStatus(User.UserStatus status);
    
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);
    
    List<User> findByExpiresAtBefore(LocalDateTime dateTime);
    
    List<User> findByExpiresAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:search% OR u.phone LIKE %:search%")
    Page<User> findByEmailContainingOrPhoneContaining(
        @Param("search") String emailSearch, 
        @Param("search") String phoneSearch, 
        Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.status = :status AND (u.email LIKE %:search% OR u.phone LIKE %:search%)")
    Page<User> findByStatusAndEmailContainingOrPhoneContaining(
        @Param("status") User.UserStatus status,
        @Param("search") String emailSearch, 
        @Param("search") String phoneSearch, 
        Pageable pageable);
}
