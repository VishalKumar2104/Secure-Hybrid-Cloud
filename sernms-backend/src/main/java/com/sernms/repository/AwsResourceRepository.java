package com.sernms.repository;

import com.sernms.entity.AwsResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AwsResourceRepository extends JpaRepository<AwsResource, Long> {
    Optional<AwsResource> findByResourceId(String resourceId);
    List<AwsResource> findByResourceType(String resourceType);
    List<AwsResource> findByStatus(String status);
    List<AwsResource> findByRegion(String region);
    long countByResourceType(String resourceType);
    long countByStatus(String status);
}
