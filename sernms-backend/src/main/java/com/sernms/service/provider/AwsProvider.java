package com.sernms.service.provider;

import com.sernms.dto.AwsResourceDto;
import com.sernms.dto.TelemetryPointDto;

import java.util.List;

public interface AwsProvider {
    List<AwsResourceDto> getResources();
    AwsResourceDto getResource(String resourceId);
    void startInstance(String resourceId);
    void stopInstance(String resourceId);
    List<TelemetryPointDto> getResourceMetrics(String resourceId);
    String getVpnStatus();
    void setVpnStatus(String status);
    void simulateCpuSpike(String resourceId, double cpuPercentage);
}
