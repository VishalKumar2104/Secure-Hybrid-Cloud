package com.sernms.repository;

import com.sernms.entity.NetworkDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, Long> {
    Optional<NetworkDevice> findByName(String name);
    Optional<NetworkDevice> findByIpAddress(String ipAddress);
    List<NetworkDevice> findByStatus(String status);
    List<NetworkDevice> findByDeviceType(String deviceType);
    List<NetworkDevice> findByVlanId(Long vlanId);

    @Query("SELECT d FROM NetworkDevice d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.ipAddress) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.manufacturer) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<NetworkDevice> searchDevices(String query);

    long countByStatus(String status);
}
