package com.dataelf.platform.repository;

import com.dataelf.platform.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    Page<Comment> findByContentIdOrderByCreatedAtDesc(Long contentId, Pageable pageable);
    
    Long countByContentId(Long contentId);
    
    // 查询未删除的评论，置顶的在前
    Page<Comment> findByContentIdAndIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(Long contentId, Pageable pageable);
    
    // 统计未删除的评论数量
    Long countByContentIdAndIsDeletedFalse(Long contentId);
}
