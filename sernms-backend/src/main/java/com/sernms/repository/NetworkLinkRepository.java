package com.sernms.repository;

import com.sernms.entity.NetworkLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkLinkRepository extends JpaRepository<NetworkLink, Long> {
    List<NetworkLink> findBySourceDeviceIdOrTargetDeviceId(Long sourceDeviceId, Long targetDeviceId);
    List<NetworkLink> findByStatus(String status);
}
