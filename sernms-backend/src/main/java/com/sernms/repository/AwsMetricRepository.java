package com.sernms.repository;

import com.sernms.entity.AwsMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AwsMetricRepository extends JpaRepository<AwsMetric, Long> {
    List<AwsMetric> findByResourceIdAndMetricNameOrderByTimestampAsc(String resourceId, String metricName);
    List<AwsMetric> findByResourceIdAndTimestampAfterOrderByTimestampAsc(String resourceId, LocalDateTime after);
}
