package com.dataelf.platform.repository;

import com.dataelf.platform.entity.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    
    Optional<UserInteraction> findByUserIdAndContentIdAndInteractionType(
        Long userId, Long contentId, UserInteraction.InteractionType interactionType
    );
    
    List<UserInteraction> findByUserIdAndInteractionType(
        Long userId, UserInteraction.InteractionType interactionType
    );
    
    Long countByContentIdAndInteractionType(
        Long contentId, UserInteraction.InteractionType interactionType
    );
}
