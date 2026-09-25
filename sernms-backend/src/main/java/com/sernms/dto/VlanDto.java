package com.sernms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VlanDto {
    private Long id;

    @NotNull(message = "VLAN ID is required")
    private Integer vlanId;

    @NotBlank(message = "VLAN Name is required")
    private String name;

    @NotBlank(message = "Subnet CIDR is required")
    private String subnetCidr;

    @NotBlank(message = "Gateway IP is required")
    private String gatewayIp;

    private String description;
    private long deviceCount;

    public VlanDto() {}

    public VlanDto(Long id, Integer vlanId, String name, String subnetCidr, String gatewayIp, String description, long deviceCount) {
        this.id = id;
        this.vlanId = vlanId;
        this.name = name;
        this.subnetCidr = subnetCidr;
        this.gatewayIp = gatewayIp;
        this.description = description;
        this.deviceCount = deviceCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVlanId() { return vlanId; }
    public void setVlanId(Integer vlanId) { this.vlanId = vlanId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubnetCidr() { return subnetCidr; }
    public void setSubnetCidr(String subnetCidr) { this.subnetCidr = subnetCidr; }

    public String getGatewayIp() { return gatewayIp; }
    public void setGatewayIp(String gatewayIp) { this.gatewayIp = gatewayIp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDeviceCount() { return deviceCount; }
    public void setDeviceCount(long deviceCount) { this.deviceCount = deviceCount; }
}
