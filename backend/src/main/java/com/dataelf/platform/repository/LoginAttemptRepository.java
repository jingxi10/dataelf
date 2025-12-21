package com.dataelf.platform.repository;

import com.dataelf.platform.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    
    /**
     * 查询指定时间范围内的失败登录尝试
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email " +
           "AND la.successful = false AND la.createdAt > :since")
    List<LoginAttempt> findFailedAttemptsSince(
        @Param("email") String email, 
        @Param("since") LocalDateTime since
    );
    
    /**
     * 查询最近一次成功登录
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email " +
           "AND la.successful = true ORDER BY la.createdAt DESC")
    List<LoginAttempt> findLastSuccessfulLogin(@Param("email") String email);
    
    /**
     * 删除指定时间之前的记录（用于清理）
     */
    void deleteByCreatedAtBefore(LocalDateTime before);
}
