package com.sernms.repository;

import com.sernms.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByTicketNumber(String ticketNumber);
    List<Incident> findByStatus(String status);
    List<Incident> findByPriority(String priority);
    List<Incident> findByCategory(String category);
    List<Incident> findByReporterId(Long reporterId);
    List<Incident> findByAssigneeId(Long assigneeId);

    @Query("SELECT i FROM Incident i ORDER BY i.createdAt DESC")
    List<Incident> findRecentIncidents();

    long countByStatus(String status);
    long countByPriority(String priority);
}
