package com.dataelf.platform.repository;

import com.dataelf.platform.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {
    
    /**
     * 查找启用的数据源
     */
    List<DataSource> findByEnabledTrue();
    
    /**
     * 查找指定状态的数据源
     */
    List<DataSource> findByStatus(DataSource.SourceStatus status);
    
    /**
     * 查找需要抓取的数据源
     */
    @Query("SELECT ds FROM DataSource ds WHERE ds.enabled = true " +
           "AND ds.status != 'DISABLED' " +
           "AND (ds.nextFetchTime IS NULL OR ds.nextFetchTime <= :now)")
    List<DataSource> findDataSourcesNeedingFetch(LocalDateTime now);
    
    /**
     * 按类型查找数据源
     */
    List<DataSource> findBySourceType(DataSource.SourceType sourceType);
}
