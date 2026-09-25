package com.sernms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "network_interfaces")
public class NetworkInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnore
    private NetworkDevice device;

    @Column(name = "interface_name", nullable = false, length = 50)
    private String interfaceName; // e.g. GigabitEthernet0/0, Fa0/1

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "mac_address", length = 30)
    private String macAddress;

    @Column(length = 20)
    private String speed = "1 Gbps";

    @Column(nullable = false, length = 20)
    private String status = "UP"; // UP, DOWN

    public NetworkInterface() {}

    public NetworkInterface(NetworkDevice device, String interfaceName, String ipAddress, String macAddress, String speed, String status) {
        this.device = device;
        this.interfaceName = interfaceName;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.speed = speed;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NetworkDevice getDevice() { return device; }
    public void setDevice(NetworkDevice device) { this.device = device; }

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
