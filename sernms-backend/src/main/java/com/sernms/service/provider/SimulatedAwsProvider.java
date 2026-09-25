package com.sernms.service.provider;

import com.sernms.dto.AwsResourceDto;
import com.sernms.dto.TelemetryPointDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulatedAwsProvider implements AwsProvider {

    private final Map<String, AwsResourceDto> resources = new ConcurrentHashMap<>();
    private final Map<String, Double> dynamicCpuOverrides = new ConcurrentHashMap<>();
    private String hybridVpnStatus = "CONNECTED";

    public SimulatedAwsProvider() {
        initializeResources();
    }

    private void initializeResources() {
        // 1. EC2 App Server
        AwsResourceDto ec2App = new AwsResourceDto();
        ec2App.setId(1L);
        ec2App.setResourceId("i-07f9c81a2b");
        ec2App.setResourceName("SERNMS-App-Server-01");
        ec2App.setResourceType("EC2");
        ec2App.setRegion("us-east-1");
        ec2App.setVpcId("vpc-01a2b3c4d5");
        ec2App.setSubnetId("subnet-0a1b2c3-app-az1");
        ec2App.setPrivateIp("10.0.10.15");
        ec2App.setPublicIp(null); // Private subnet!
        ec2App.setStatus("RUNNING");
        ec2App.setInstanceType("t3.large (2 vCPU, 8 GB RAM)");
        ec2App.setStorageInfo("60 GB gp3 NVMe EBS");
        ec2App.setLaunchTime(LocalDateTime.now().minusDays(45));
        ec2App.setLastSync(LocalDateTime.now());
        ec2App.setCurrentCpuUtilization(32.4);
        resources.put(ec2App.getResourceId(), ec2App);

        // 2. EC2 Worker Node
        AwsResourceDto ec2Worker = new AwsResourceDto();
        ec2Worker.setId(2L);
        ec2Worker.setResourceId("i-03b2c1d4e5");
        ec2Worker.setResourceName("SERNMS-Worker-Node-02");
        ec2Worker.setResourceType("EC2");
        ec2Worker.setRegion("us-east-1");
        ec2Worker.setVpcId("vpc-01a2b3c4d5");
        ec2Worker.setSubnetId("subnet-0d4e5f6-app-az2");
        ec2Worker.setPrivateIp("10.0.11.22");
        ec2Worker.setPublicIp(null);
        ec2Worker.setStatus("RUNNING");
        ec2Worker.setInstanceType("t3.medium (2 vCPU, 4 GB RAM)");
        ec2Worker.setStorageInfo("40 GB gp3 NVMe EBS");
        ec2Worker.setLaunchTime(LocalDateTime.now().minusDays(30));
        ec2Worker.setLastSync(LocalDateTime.now());
        ec2Worker.setCurrentCpuUtilization(18.2);
        resources.put(ec2Worker.getResourceId(), ec2Worker);

        // 3. RDS MySQL Multi-AZ
        AwsResourceDto rdsDb = new AwsResourceDto();
        rdsDb.setId(3L);
        rdsDb.setResourceId("rds-mysql-prod-01");
        rdsDb.setResourceName("SERNMS-RDS-MySQL-MultiAZ");
        rdsDb.setResourceType("RDS");
        rdsDb.setRegion("us-east-1");
        rdsDb.setVpcId("vpc-01a2b3c4d5");
        rdsDb.setSubnetId("subnet-db-az1 (10.0.20.0/24)");
        rdsDb.setPrivateIp("10.0.20.45");
        rdsDb.setPublicIp(null);
        rdsDb.setStatus("AVAILABLE");
        rdsDb.setInstanceType("db.t3.medium (MySQL 8.0.35 Multi-AZ)");
        rdsDb.setStorageInfo("100 GB gp3 Provisioned (Autoscale to 500GB)");
        rdsDb.setLaunchTime(LocalDateTime.now().minusDays(60));
        rdsDb.setLastSync(LocalDateTime.now());
        rdsDb.setCurrentCpuUtilization(24.5);
        resources.put(rdsDb.getResourceId(), rdsDb);

        // 4. S3 Audit Reports Bucket
        AwsResourceDto s3Reports = new AwsResourceDto();
        s3Reports.setId(4L);
        s3Reports.setResourceId("s3-sernms-audit-reports");
        s3Reports.setResourceName("sernms-audit-reports-us-east-1");
        s3Reports.setResourceType("S3");
        s3Reports.setRegion("us-east-1");
        s3Reports.setStatus("AVAILABLE");
        s3Reports.setInstanceType("Amazon S3 Standard (SSE-S3 AES-256)");
        s3Reports.setStorageInfo("318 Objects / 2.45 GB");
        s3Reports.setLaunchTime(LocalDateTime.now().minusDays(90));
        s3Reports.setLastSync(LocalDateTime.now());
        resources.put(s3Reports.getResourceId(), s3Reports);

        // 5. S3 DB Backups Bucket
        AwsResourceDto s3Backups = new AwsResourceDto();
        s3Backups.setId(5L);
        s3Backups.setResourceId("s3-sernms-db-backups");
        s3Backups.setResourceName("sernms-db-snapshots-backup");
        s3Backups.setResourceType("S3");
        s3Backups.setRegion("us-east-1");
        s3Backups.setStatus("AVAILABLE");
        s3Backups.setInstanceType("Amazon S3 Standard-Infrequent Access");
        s3Backups.setStorageInfo("18 Snapshots / 42.1 GB");
        s3Backups.setLaunchTime(LocalDateTime.now().minusDays(90));
        s3Backups.setLastSync(LocalDateTime.now());
        resources.put(s3Backups.getResourceId(), s3Backups);

        // 6. Application Load Balancer
        AwsResourceDto alb = new AwsResourceDto();
        alb.setId(6L);
        alb.setResourceId("alb-sernms-prod");
        alb.setResourceName("SERNMS-Internet-Facing-ALB");
        alb.setResourceType("ALB");
        alb.setRegion("us-east-1");
        alb.setVpcId("vpc-01a2b3c4d5");
        alb.setPublicIp("54.210.88.192");
        alb.setStatus("HEALTHY");
        alb.setInstanceType("Application Load Balancer (Dual-AZ TLS 1.3)");
        alb.setStorageInfo("Target Group: 2 Healthy Targets (:8080)");
        alb.setLaunchTime(LocalDateTime.now().minusDays(45));
        alb.setLastSync(LocalDateTime.now());
        resources.put(alb.getResourceId(), alb);

        // 7. Virtual Private Gateway (VPN)
        AwsResourceDto vgw = new AwsResourceDto();
        vgw.setId(7L);
        vgw.setResourceId("vgw-sernms-hybrid-01");
        vgw.setResourceName("SERNMS-Campus-IPSec-VGW");
        vgw.setResourceType("VGW");
        vgw.setRegion("us-east-1");
        vgw.setVpcId("vpc-01a2b3c4d5");
        vgw.setStatus("CONNECTED");
        vgw.setInstanceType("AWS Virtual Private Gateway (IKEv2 IPSec AES-256)");
        vgw.setStorageInfo("Peer Gateway: 203.0.113.1 (R-EDGE-01)");
        vgw.setLaunchTime(LocalDateTime.now().minusDays(45));
        vgw.setLastSync(LocalDateTime.now());
        resources.put(vgw.getResourceId(), vgw);
    }

    @Override
    public List<AwsResourceDto> getResources() {
        return new ArrayList<>(resources.values());
    }

    @Override
    public AwsResourceDto getResource(String resourceId) {
        return resources.get(resourceId);
    }

    @Override
    public void startInstance(String resourceId) {
        AwsResourceDto res = resources.get(resourceId);
        if (res != null) {
            res.setStatus("RUNNING");
            res.setLastSync(LocalDateTime.now());
        }
    }

    @Override
    public void stopInstance(String resourceId) {
        AwsResourceDto res = resources.get(resourceId);
        if (res != null) {
            res.setStatus("STOPPED");
            res.setLastSync(LocalDateTime.now());
        }
    }

    @Override
    public List<TelemetryPointDto> getResourceMetrics(String resourceId) {
        List<TelemetryPointDto> points = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();

        double baseCpu = dynamicCpuOverrides.getOrDefault(resourceId, 32.0);

        for (int i = 9; i >= 0; i--) {
            LocalDateTime t = now.minusMinutes(i * 3);
            double cpuNoise = (Math.random() * 6.0) - 3.0;
            double cpu = Math.max(5.0, Math.min(99.0, baseCpu + cpuNoise));
            double mem = Math.max(20.0, Math.min(95.0, 48.0 + (Math.random() * 4.0)));
            double netIn = Math.max(1.0, 12.5 + (Math.random() * 8.0));
            double netOut = Math.max(1.0, 18.2 + (Math.random() * 10.0));

            points.add(new TelemetryPointDto(
                    t.format(timeFmt),
                    Math.round(cpu * 10.0) / 10.0,
                    Math.round(mem * 10.0) / 10.0,
                    Math.round(netIn * 10.0) / 10.0,
                    Math.round(netOut * 10.0) / 10.0
            ));
        }
        return points;
    }

    @Override
    public String getVpnStatus() {
        return hybridVpnStatus;
    }

    @Override
    public void setVpnStatus(String status) {
        this.hybridVpnStatus = status;
        AwsResourceDto vgw = resources.get("vgw-sernms-hybrid-01");
        if (vgw != null) {
            vgw.setStatus(status);
        }
    }

    @Override
    public void simulateCpuSpike(String resourceId, double cpuPercentage) {
        dynamicCpuOverrides.put(resourceId, cpuPercentage);
        AwsResourceDto res = resources.get(resourceId);
        if (res != null) {
            res.setCurrentCpuUtilization(cpuPercentage);
        }
    }
}
