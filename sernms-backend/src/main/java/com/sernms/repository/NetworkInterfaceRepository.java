package com.sernms.repository;

import com.sernms.entity.NetworkInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkInterfaceRepository extends JpaRepository<NetworkInterface, Long> {
    List<NetworkInterface> findByDeviceId(Long deviceId);
}
