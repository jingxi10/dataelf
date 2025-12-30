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
    
    // 按用户ID统计
    long countByUserId(Long userId);
    
    long countByUserIdAndStatus(Long userId, Content.ContentStatus status);
    
    // 搜索已发布内容（标题或作者名包含关键词）
    @Query("SELECT c FROM Content c WHERE c.status = :status AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.authorName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Content> searchByKeyword(@Param("keyword") String keyword, @Param("status") Content.ContentStatus status, Pageable pageable);
    
    // 查询指定管理员审核的内容
    Page<Content> findByReviewedBy(Long reviewedBy, Pageable pageable);
    
    // 统计指定管理员审核的内容数量
    long countByReviewedBy(Long reviewedBy);
    
    // 统计指定管理员审核通过的内容数量
    long countByReviewedByAndStatus(Long reviewedBy, Content.ContentStatus status);
    
    // 查询指定管理员审核的内容（按状态筛选）
    Page<Content> findByReviewedByAndStatus(Long reviewedBy, Content.ContentStatus status, Pageable pageable);
    
    // 查询所有已审核的内容（reviewedBy不为null）
    @Query("SELECT c FROM Content c WHERE c.reviewedBy IS NOT NULL ORDER BY c.reviewedAt DESC")
    Page<Content> findAllReviewed(Pageable pageable);
    
    // 按标题或作者名搜索（所有状态）
    @Query("SELECT c FROM Content c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.authorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Content> findByTitleContainingOrAuthorNameContaining(@Param("keyword") String keyword1, @Param("keyword") String keyword2, Pageable pageable);
    
    // 按状态和标题或作者名搜索
    @Query("SELECT c FROM Content c WHERE c.status = :status AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.authorName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Content> findByStatusAndTitleContainingOrAuthorNameContaining(@Param("status") Content.ContentStatus status, @Param("keyword") String keyword1, @Param("keyword") String keyword2, Pageable pageable);
}
