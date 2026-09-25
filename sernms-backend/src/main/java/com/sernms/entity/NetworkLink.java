package com.sernms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "network_links")
public class NetworkLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_device_id", nullable = false)
    private Long sourceDeviceId;

    @Column(name = "target_device_id", nullable = false)
    private Long targetDeviceId;

    @Column(name = "source_interface", length = 50)
    private String sourceInterface;

    @Column(name = "target_interface", length = 50)
    private String targetInterface;

    @Column(name = "link_type", length = 30)
    private String linkType = "ETHERNET"; // ETHERNET, FIBER, VPN_TUNNEL

    @Column(length = 20)
    private String bandwidth = "1 Gbps";

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, DEGRADED, DOWN

    public NetworkLink() {}

    public NetworkLink(Long sourceDeviceId, Long targetDeviceId, String sourceInterface,
                       String targetInterface, String linkType, String bandwidth, String status) {
        this.sourceDeviceId = sourceDeviceId;
        this.targetDeviceId = targetDeviceId;
        this.sourceInterface = sourceInterface;
        this.targetInterface = targetInterface;
        this.linkType = linkType;
        this.bandwidth = bandwidth;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSourceDeviceId() { return sourceDeviceId; }
    public void setSourceDeviceId(Long sourceDeviceId) { this.sourceDeviceId = sourceDeviceId; }

    public Long getTargetDeviceId() { return targetDeviceId; }
    public void setTargetDeviceId(Long targetDeviceId) { this.targetDeviceId = targetDeviceId; }

    public String getSourceInterface() { return sourceInterface; }
    public void setSourceInterface(String sourceInterface) { this.sourceInterface = sourceInterface; }

    public String getTargetInterface() { return targetInterface; }
    public void setTargetInterface(String targetInterface) { this.targetInterface = targetInterface; }

    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }

    public String getBandwidth() { return bandwidth; }
    public void setBandwidth(String bandwidth) { this.bandwidth = bandwidth; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
