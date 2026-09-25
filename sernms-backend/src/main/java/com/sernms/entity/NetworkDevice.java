package com.sernms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "network_devices")
public class NetworkDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "device_type", nullable = false, length = 30)
    private String deviceType; // ROUTER, SWITCH, FIREWALL, VPN_GATEWAY, ACCESS_POINT, SERVER

    @Column(name = "ip_address", nullable = false, unique = true, length = 45)
    private String ipAddress;

    @Column(name = "mac_address", length = 30)
    private String macAddress;

    @Column(length = 100)
    private String location;

    @Column(length = 50)
    private String manufacturer; // Cisco, Fortinet, Juniper, HP, Dell

    @Column(length = 50)
    private String model;

    @Column(nullable = false, length = 20)
    private String status = "ONLINE"; // ONLINE, OFFLINE, WARNING, MAINTENANCE

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vlan_id")
    private Vlan vlan;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen = LocalDateTime.now();

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("device")
    private List<NetworkInterface> interfaces = new ArrayList<>();

    public NetworkDevice() {}

    public NetworkDevice(String name, String deviceType, String ipAddress, String macAddress,
                         String location, String manufacturer, String model, String status,
                         Vlan vlan, String description) {
        this.name = name;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.location = location;
        this.manufacturer = manufacturer;
        this.model = model;
        this.status = status;
        this.vlan = vlan;
        this.description = description;
        this.lastSeen = LocalDateTime.now();
    }

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

    public Vlan getVlan() { return vlan; }
    public void setVlan(Vlan vlan) { this.vlan = vlan; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<NetworkInterface> getInterfaces() { return interfaces; }
    public void setInterfaces(List<NetworkInterface> interfaces) { this.interfaces = interfaces; }
}
