package com.sernms.dto;

public class DemoSimulationRequest {
    private String scenario; // DEVICE_FAIL, DEVICE_RESTORE, HIGH_CPU, VPN_FAIL, VPN_RESTORE
    private String targetId; // e.g. R-EDGE-01, SW-CORE-01, i-07f9c81a2b
    private String status; // OFFLINE, ONLINE, WARNING
    private Double cpuValue;

    public DemoSimulationRequest() {}

    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getCpuValue() { return cpuValue; }
    public void setCpuValue(Double cpuValue) { this.cpuValue = cpuValue; }
}
