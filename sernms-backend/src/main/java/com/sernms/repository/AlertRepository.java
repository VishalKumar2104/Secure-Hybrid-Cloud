package com.sernms.repository;

import com.sernms.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByAcknowledgedFalseOrderByTriggeredAtDesc();
    List<Alert> findBySeverity(String severity);
    List<Alert> findByCategory(String category);

    @Query("SELECT a FROM Alert a ORDER BY a.triggeredAt DESC")
    List<Alert> findRecentAlerts();

    long countByAcknowledgedFalse();
    long countBySeverityAndAcknowledgedFalse(String severity);
}
