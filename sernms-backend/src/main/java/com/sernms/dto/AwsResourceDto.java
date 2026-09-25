package com.sernms.dto;

import java.time.LocalDateTime;

public class AwsResourceDto {
    private Long id;
    private String resourceId;
    private String resourceName;
    private String resourceType; // EC2, RDS, S3, VPC, SUBNET, ALB, VGW
    private String region;
    private String vpcId;
    private String subnetId;
    private String privateIp;
    private String publicIp;
    private String status;
    private String instanceType;
    private String storageInfo;
    private LocalDateTime launchTime;
    private LocalDateTime lastSync;
    private Double currentCpuUtilization;

    public AwsResourceDto() {}

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

    public Double getCurrentCpuUtilization() { return currentCpuUtilization; }
    public void setCurrentCpuUtilization(Double currentCpuUtilization) { this.currentCpuUtilization = currentCpuUtilization; }
}
