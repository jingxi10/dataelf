package com.dataelf.platform.repository;

import com.dataelf.platform.entity.Content;
import com.dataelf.platform.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    
    Page<Content> findByUserId(Long userId, Pageable pageable);
    
    Page<Content> findByStatus(Content.ContentStatus status, Pageable pageable);
    
    Page<Content> findByStatusOrderByPublishedAtDesc(Content.ContentStatus status, Pageable pageable);
    
    List<Content> findByStatusAndUserId(Content.ContentStatus status, Long userId);
    
    List<Content> findByStatus(Content.ContentStatus status);
    
    Page<Content> findByTagsContainingAndStatus(Tag tag, Content.ContentStatus status, Pageable pageable);
    
    Page<Content> findByUserIdAndStatus(Long userId, Content.ContentStatus status, Pageable pageable);
    
    Page<Content> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 搜索已发布内容（标题或作者名包含关键词）
    @Query("SELECT c FROM Content c WHERE c.status = :status AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.authorName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Content> searchByKeyword(@Param("keyword") String keyword, @Param("status") Content.ContentStatus status, Pageable pageable);
}
