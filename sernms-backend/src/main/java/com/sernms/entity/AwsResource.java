package com.sernms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aws_resources")
public class AwsResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id", nullable = false, unique = true, length = 100)
    private String resourceId; // e.g. i-0a1b2c3d4e, rds-mysql-prod, s3-sernms-reports

    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;

    @Column(name = "resource_type", nullable = false, length = 30)
    private String resourceType; // EC2, RDS, S3, VPC, SUBNET, ALB, VGW

    @Column(nullable = false, length = 30)
    private String region = "us-east-1";

    @Column(name = "vpc_id", length = 50)
    private String vpcId;

    @Column(name = "subnet_id", length = 50)
    private String subnetId;

    @Column(name = "private_ip", length = 45)
    private String privateIp;

    @Column(name = "public_ip", length = 45)
    private String publicIp;

    @Column(nullable = false, length = 30)
    private String status = "AVAILABLE"; // RUNNING, STOPPED, AVAILABLE, HEALTHY, TERMINATED

    @Column(name = "instance_type", length = 50)
    private String instanceType; // t3.medium, db.t3.micro, Standard

    @Column(name = "storage_info", length = 100)
    private String storageInfo; // 50 GB gp3, 120 Objects

    @Column(name = "launch_time")
    private LocalDateTime launchTime;

    @Column(name = "last_sync")
    private LocalDateTime lastSync = LocalDateTime.now();

    public AwsResource() {}

    public AwsResource(String resourceId, String resourceName, String resourceType, String region,
                       String vpcId, String subnetId, String privateIp, String publicIp,
                       String status, String instanceType, String storageInfo) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.region = region;
        this.vpcId = vpcId;
        this.subnetId = subnetId;
        this.privateIp = privateIp;
        this.publicIp = publicIp;
        this.status = status;
        this.instanceType = instanceType;
        this.storageInfo = storageInfo;
        this.launchTime = LocalDateTime.now();
        this.lastSync = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getVpcId() { return vpcId; }
    public void setVpcId(String vpcId) { this.vpcId = vpcId; }

    public String getSubnetId() { return subnetId; }
    public void setSubnetId(String subnetId) { this.subnetId = subnetId; }

    public String getPrivateIp() { return privateIp; }
    public void setPrivateIp(String privateIp) { this.privateIp = privateIp; }

    public String getPublicIp() { return publicIp; }
    public void setPublicIp(String publicIp) { this.publicIp = publicIp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInstanceType() { return instanceType; }
    public void setInstanceType(String instanceType) { this.instanceType = instanceType; }

    public String getStorageInfo() { return storageInfo; }
    public void setStorageInfo(String storageInfo) { this.storageInfo = storageInfo; }

    public LocalDateTime getLaunchTime() { return launchTime; }
    public void setLaunchTime(LocalDateTime launchTime) { this.launchTime = launchTime; }

    public LocalDateTime getLastSync() { return lastSync; }
    public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }
}
