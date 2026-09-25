package com.sernms.dto;

public class TelemetryPointDto {
    private String timeLabel;
    private double cpuUtilization;
    private double memoryUtilization;
    private double networkInMbps;
    private double networkOutMbps;

    public TelemetryPointDto() {}

    public TelemetryPointDto(String timeLabel, double cpuUtilization, double memoryUtilization, double networkInMbps, double networkOutMbps) {
        this.timeLabel = timeLabel;
        this.cpuUtilization = cpuUtilization;
        this.memoryUtilization = memoryUtilization;
        this.networkInMbps = networkInMbps;
        this.networkOutMbps = networkOutMbps;
    }

    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }

    public double getCpuUtilization() { return cpuUtilization; }
    public void setCpuUtilization(double cpuUtilization) { this.cpuUtilization = cpuUtilization; }

    public double getMemoryUtilization() { return memoryUtilization; }
    public void setMemoryUtilization(double memoryUtilization) { this.memoryUtilization = memoryUtilization; }

    public double getNetworkInMbps() { return networkInMbps; }
    public void setNetworkInMbps(double networkInMbps) { this.networkInMbps = networkInMbps; }

    public double getNetworkOutMbps() { return networkOutMbps; }
    public void setNetworkOutMbps(double networkOutMbps) { this.networkOutMbps = networkOutMbps; }
}
