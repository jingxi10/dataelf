package com.dataelf.platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_interactions", 
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_interaction", columnNames = {"user_id", "content_id", "interaction_type"})
    },
    indexes = {
        @Index(name = "idx_content_id", columnList = "content_id"),
        @Index(name = "idx_interaction_type", columnList = "interaction_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteraction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "content_id", nullable = false)
    private Long contentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false, length = 20)
    private InteractionType interactionType;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum InteractionType {
        LIKE,
        FAVORITE,
        SHARE
    }
}
