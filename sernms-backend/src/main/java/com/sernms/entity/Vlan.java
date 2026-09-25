package com.sernms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vlans")
public class Vlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vlan_id", nullable = false, unique = true)
    private Integer vlanId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "subnet_cidr", nullable = false, length = 30)
    private String subnetCidr;

    @Column(name = "gateway_ip", nullable = false, length = 30)
    private String gatewayIp;

    @Column(length = 255)
    private String description;

    public Vlan() {}

    public Vlan(Integer vlanId, String name, String subnetCidr, String gatewayIp, String description) {
        this.vlanId = vlanId;
        this.name = name;
        this.subnetCidr = subnetCidr;
        this.gatewayIp = gatewayIp;
        this.description = description;
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
}
