package com.sernms.service.provider;

import com.sernms.dto.AwsResourceDto;
import com.sernms.dto.TelemetryPointDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class LiveAwsProvider implements AwsProvider {

    private static final Logger log = LoggerFactory.getLogger(LiveAwsProvider.class);

    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final SimulatedAwsProvider fallbackSimulator;

    public LiveAwsProvider(
            @Value("${app.aws.region:us-east-1}") String region,
            @Value("${app.aws.access-key-id:}") String accessKey,
            @Value("${app.aws.secret-access-key:}") String secretKey,
            SimulatedAwsProvider fallbackSimulator) {
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.fallbackSimulator = fallbackSimulator;
    }

    private boolean isConfigured() {
        return accessKey != null && !accessKey.trim().isEmpty() &&
               secretKey != null && !secretKey.trim().isEmpty();
    }

    private StaticCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Override
    public List<AwsResourceDto> getResources() {
        if (!isConfigured()) {
            log.info("AWS Credentials not supplied. Falling back to SimulatedAwsProvider.");
            return fallbackSimulator.getResources();
        }

        List<AwsResourceDto> list = new ArrayList<>();
        try (Ec2Client ec2 = Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build()) {

            DescribeInstancesResponse response = ec2.describeInstances();
            for (Reservation reservation : response.reservations()) {
                for (Instance inst : reservation.instances()) {
                    AwsResourceDto dto = new AwsResourceDto();
                    dto.setResourceId(inst.instanceId());
                    dto.setResourceName(inst.tags().stream()
                            .filter(t -> "Name".equalsIgnoreCase(t.key()))
                            .map(Tag::value)
                            .findFirst().orElse(inst.instanceId()));
                    dto.setResourceType("EC2");
                    dto.setRegion(region);
                    dto.setVpcId(inst.vpcId());
                    dto.setSubnetId(inst.subnetId());
                    dto.setPrivateIp(inst.privateIpAddress());
                    dto.setPublicIp(inst.publicIpAddress());
                    dto.setStatus(inst.state().nameAsString().toUpperCase());
                    dto.setInstanceType(inst.instanceTypeAsString());
                    dto.setLaunchTime(LocalDateTime.now());
                    dto.setLastSync(LocalDateTime.now());
                    list.add(dto);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to query live AWS EC2 SDK: {}. Using simulated backup.", ex.getMessage());
            return fallbackSimulator.getResources();
        }

        try (RdsClient rds = RdsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build()) {

            DescribeDbInstancesResponse rdsResponse = rds.describeDBInstances();
            for (DBInstance db : rdsResponse.dbInstances()) {
                AwsResourceDto dto = new AwsResourceDto();
                dto.setResourceId(db.dbInstanceIdentifier());
                dto.setResourceName(db.dbInstanceIdentifier());
                dto.setResourceType("RDS");
                dto.setRegion(region);
                dto.setStatus(db.dbInstanceStatus().toUpperCase());
                dto.setInstanceType(db.dbInstanceClass() + " (" + db.engine() + ")");
                dto.setStorageInfo(db.allocatedStorage() + " GB " + db.storageType());
                dto.setLastSync(LocalDateTime.now());
                list.add(dto);
            }
        } catch (Exception ex) {
            log.warn("Failed to query live AWS RDS: {}", ex.getMessage());
        }

        try (S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build()) {

            for (Bucket b : s3.listBuckets().buckets()) {
                AwsResourceDto dto = new AwsResourceDto();
                dto.setResourceId(b.name());
                dto.setResourceName(b.name());
                dto.setResourceType("S3");
                dto.setRegion(region);
                dto.setStatus("AVAILABLE");
                dto.setInstanceType("Amazon S3 Bucket");
                dto.setLastSync(LocalDateTime.now());
                list.add(dto);
            }
        } catch (Exception ex) {
            log.warn("Failed to query live AWS S3: {}", ex.getMessage());
        }

        return list.isEmpty() ? fallbackSimulator.getResources() : list;
    }

    @Override
    public AwsResourceDto getResource(String resourceId) {
        return fallbackSimulator.getResource(resourceId);
    }

    @Override
    public void startInstance(String resourceId) {
        if (!isConfigured()) {
            fallbackSimulator.startInstance(resourceId);
            return;
        }
        try (Ec2Client ec2 = Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build()) {
            ec2.startInstances(StartInstancesRequest.builder().instanceIds(resourceId).build());
        } catch (Exception ex) {
            log.error("Live EC2 start failed: {}", ex.getMessage());
            fallbackSimulator.startInstance(resourceId);
        }
    }

    @Override
    public void stopInstance(String resourceId) {
        if (!isConfigured()) {
            fallbackSimulator.stopInstance(resourceId);
            return;
        }
        try (Ec2Client ec2 = Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build()) {
            ec2.stopInstances(StopInstancesRequest.builder().instanceIds(resourceId).build());
        } catch (Exception ex) {
            log.error("Live EC2 stop failed: {}", ex.getMessage());
            fallbackSimulator.stopInstance(resourceId);
        }
    }

    @Override
    public List<TelemetryPointDto> getResourceMetrics(String resourceId) {
        return fallbackSimulator.getResourceMetrics(resourceId);
    }

    @Override
    public String getVpnStatus() {
        return fallbackSimulator.getVpnStatus();
    }

    @Override
    public void setVpnStatus(String status) {
        fallbackSimulator.setVpnStatus(status);
    }

    @Override
    public void simulateCpuSpike(String resourceId, double cpuPercentage) {
        fallbackSimulator.simulateCpuSpike(resourceId, cpuPercentage);
    }
}
