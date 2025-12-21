package com.dataelf.platform.repository;

import com.dataelf.platform.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    
    List<Template> findByType(String type);
    
    List<Template> findByIsSystem(Boolean isSystem);
    
    @Query("SELECT COUNT(c) FROM Content c WHERE c.templateId = :templateId")
    Long countContentsByTemplateId(Long templateId);
}
