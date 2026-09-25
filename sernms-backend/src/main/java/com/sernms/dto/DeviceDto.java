package com.sernms.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class DeviceDto {
    private Long id;

    @NotBlank(message = "Device name is required")
    private String name;

    @NotBlank(message = "Device type is required")
    private String deviceType; // ROUTER, SWITCH, FIREWALL, VPN_GATEWAY, ACCESS_POINT, SERVER

    @NotBlank(message = "IP address is required")
    private String ipAddress;

    private String macAddress;
    private String location;
    private String manufacturer;
    private String model;
    private String status = "ONLINE"; // ONLINE, OFFLINE, WARNING, MAINTENANCE
    private Long vlanId;
    private String vlanName;
    private Integer vlanNumber;
    private LocalDateTime lastSeen;
    private String description;
    private List<InterfaceDto> interfaces;

    public DeviceDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVlanId() { return vlanId; }
    public void setVlanId(Long vlanId) { this.vlanId = vlanId; }

    public String getVlanName() { return vlanName; }
    public void setVlanName(String vlanName) { this.vlanName = vlanName; }

    public Integer getVlanNumber() { return vlanNumber; }
    public void setVlanNumber(Integer vlanNumber) { this.vlanNumber = vlanNumber; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<InterfaceDto> getInterfaces() { return interfaces; }
    public void setInterfaces(List<InterfaceDto> interfaces) { this.interfaces = interfaces; }

    public static class InterfaceDto {
        private Long id;
        private String interfaceName;
        private String ipAddress;
        private String macAddress;
        private String speed;
        private String status;

        public InterfaceDto() {}

        public InterfaceDto(Long id, String interfaceName, String ipAddress, String macAddress, String speed, String status) {
            this.id = id;
            this.interfaceName = interfaceName;
            this.ipAddress = ipAddress;
            this.macAddress = macAddress;
            this.speed = speed;
            this.status = status;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getInterfaceName() { return interfaceName; }
        public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getMacAddress() { return macAddress; }
        public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

        public String getSpeed() { return speed; }
        public void setSpeed(String speed) { this.speed = speed; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
