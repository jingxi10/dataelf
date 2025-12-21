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
}
