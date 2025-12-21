package com.dataelf.platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contents", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_template_id", columnList = "template_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_published_at", columnList = "published_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Content {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(name = "structured_data", nullable = false, columnDefinition = "JSON")
    private String structuredData;
    
    @Column(name = "json_ld", nullable = false, columnDefinition = "TEXT")
    private String jsonLd;
    
    @Column(name = "html_output", nullable = false, columnDefinition = "TEXT")
    private String htmlOutput;
    
    @Column(name = "markdown_output", columnDefinition = "TEXT")
    private String markdownOutput;
    
    @Column(name = "copyright_notice", columnDefinition = "TEXT")
    private String copyrightNotice;
    
    @Column(name = "content_source", length = 500)
    private String contentSource;
    
    @Column(name = "author_name", length = 255)
    private String authorName;
    
    @Column(name = "is_original", nullable = false)
    private Boolean isOriginal = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status = ContentStatus.DRAFT;
    
    @Column(name = "integrity_score", precision = 3, scale = 2)
    private BigDecimal integrityScore;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @ManyToMany
    @JoinTable(
        name = "content_categories",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "content_tags",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    public enum ContentStatus {
        DRAFT,
        PENDING_REVIEW,
        APPROVED,
        REJECTED,
        PUBLISHED
    }
}
