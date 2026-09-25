package com.sernms.service;

import com.sernms.dto.TopologyEdgeDto;
import com.sernms.dto.TopologyGraphDto;
import com.sernms.dto.TopologyNodeDto;
import com.sernms.entity.NetworkDevice;
import com.sernms.repository.NetworkDeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TopologyService {

    private final NetworkDeviceRepository deviceRepository;
    private final AwsService awsService;

    public TopologyService(NetworkDeviceRepository deviceRepository, AwsService awsService) {
        this.deviceRepository = deviceRepository;
        this.awsService = awsService;
    }

    @Transactional(readOnly = true)
    public TopologyGraphDto getTopologyGraph() {
        List<TopologyNodeDto> nodes = new ArrayList<>();
        List<TopologyEdgeDto> edges = new ArrayList<>();

        String vpnStatus = awsService.getVpnStatus();

        // 1. Internet & External Gateway
        nodes.add(new TopologyNodeDto("node-internet", "Public Internet", "WAN / Global Gateway", "INTERNET", "ONLINE", "0.0.0.0/0", 1, "cloud", "#38bdf8"));

        // 2. Enterprise Edge Router (R-EDGE-01)
        NetworkDevice router = deviceRepository.findByName("R-EDGE-01").orElse(null);
        String routerStatus = router != null ? router.getStatus() : "ONLINE";
        String routerColor = getColorForStatus(routerStatus);
        nodes.add(new TopologyNodeDto("node-router", "R-EDGE-01\n(Cisco 2911)", "Edge Router & NAT Gateway\nIP: 10.10.0.1", "ROUTER", routerStatus, "10.10.0.1", 2, "box", routerColor));

        // 3. Enterprise NextGen Firewall (FW-CORE-01)
        NetworkDevice fw = deviceRepository.findByName("FW-CORE-01").orElse(null);
        String fwStatus = fw != null ? fw.getStatus() : "ONLINE";
        String fwColor = getColorForStatus(fwStatus);
        nodes.add(new TopologyNodeDto("node-firewall", "FW-CORE-01\n(Cisco ASA)", "NextGen Enterprise Firewall\nIP: 10.10.0.2", "FIREWALL", fwStatus, "10.10.0.2", 3, "box", fwColor));

        // 4. Core Switch L3 (SW-CORE-01)
        NetworkDevice coreSw = deviceRepository.findByName("SW-CORE-01").orElse(null);
        String coreSwStatus = coreSw != null ? coreSw.getStatus() : "ONLINE";
        String coreSwColor = getColorForStatus(coreSwStatus);
        nodes.add(new TopologyNodeDto("node-core-sw", "SW-CORE-01\n(Catalyst 3650)", "Core L3 Switch (Inter-VLAN)\nIP: 10.10.0.3", "SWITCH", coreSwStatus, "10.10.0.3", 4, "box", coreSwColor));

        // 5. Access Switches
        NetworkDevice acc1 = deviceRepository.findByName("SW-ACC-01").orElse(null);
        String acc1Status = acc1 != null ? acc1.getStatus() : "ONLINE";
        nodes.add(new TopologyNodeDto("node-acc-1", "SW-ACC-01\n(HR/IT)", "Access Switch 1\nIP: 10.10.0.11", "SWITCH", acc1Status, "10.10.0.11", 5, "box", getColorForStatus(acc1Status)));

        NetworkDevice acc2 = deviceRepository.findByName("SW-ACC-02").orElse(null);
        String acc2Status = acc2 != null ? acc2.getStatus() : "ONLINE";
        nodes.add(new TopologyNodeDto("node-acc-2", "SW-ACC-02\n(Finance)", "Access Switch 2\nIP: 10.10.0.12", "SWITCH", acc2Status, "10.10.0.12", 5, "box", getColorForStatus(acc2Status)));

        NetworkDevice acc3 = deviceRepository.findByName("SW-ACC-03").orElse(null);
        String acc3Status = acc3 != null ? acc3.getStatus() : "ONLINE";
        nodes.add(new TopologyNodeDto("node-acc-3", "SW-ACC-03\n(Admin/Mgmt)", "Access Switch 3\nIP: 10.10.0.13", "SWITCH", acc3Status, "10.10.0.13", 5, "box", getColorForStatus(acc3Status)));

        // 6. Enterprise Endpoints / VLAN Groups
        nodes.add(new TopologyNodeDto("node-vlan-10", "VLAN 10: HR Hosts\n(10.10.10.0/24)", "HR Department Workstations", "ENDPOINT", "ONLINE", "10.10.10.0/24", 6, "ellipse", "#a78bfa"));
        nodes.add(new TopologyNodeDto("node-vlan-20", "VLAN 20: IT Hosts\n(10.10.20.0/24)", "IT Operations Terminals", "ENDPOINT", "ONLINE", "10.10.20.0/24", 6, "ellipse", "#38bdf8"));
        nodes.add(new TopologyNodeDto("node-vlan-30", "VLAN 30: Finance\n(10.10.30.0/24)", "Accounting Terminals", "ENDPOINT", "ONLINE", "10.10.30.0/24", 6, "ellipse", "#fbbf24"));
        nodes.add(new TopologyNodeDto("node-vlan-40", "VLAN 40: Admin\n(10.10.40.0/24)", "Network Management Out-of-Band", "ENDPOINT", "ONLINE", "10.10.40.0/24", 6, "ellipse", "#34d399"));

        // 7. AWS Cloud Branch
        String vgwColor = "CONNECTED".equalsIgnoreCase(vpnStatus) ? "#22c55e" : "#ef4444";
        nodes.add(new TopologyNodeDto("node-aws-vgw", "AWS Virtual Private\nGateway (VGW)", "AWS IPSec Peer Gateway\nStatus: " + vpnStatus, "AWS_VGW", vpnStatus, "10.0.0.0/16", 2, "box", vgwColor));

        nodes.add(new TopologyNodeDto("node-aws-alb", "AWS Public ALB\n(54.210.88.192)", "Application Load Balancer\nPublic Subnets (Dual AZ)", "AWS_ALB", "ONLINE", "10.0.1.0/24", 3, "box", "#f97316"));

        nodes.add(new TopologyNodeDto("node-aws-ec2", "SERNMS App Server\n(EC2 t3.large)", "Spring Boot App Tier\nPrivate App Subnet: 10.0.10.15", "AWS_EC2", "ONLINE", "10.0.10.15", 4, "box", "#3b82f6"));

        nodes.add(new TopologyNodeDto("node-aws-rds", "RDS MySQL Multi-AZ\n(db.t3.medium)", "Isolated Private DB Subnet\nPrivate IP: 10.0.20.45", "AWS_RDS", "ONLINE", "10.0.20.45", 5, "database", "#6366f1"));

        nodes.add(new TopologyNodeDto("node-aws-s3", "Amazon S3 Storage\n(Audit & Backups)", "Encrypted Object Store", "AWS_S3", "ONLINE", "s3.amazonaws.com", 5, "database", "#14b8a6"));

        // ================= EDGES / CONNECTIONS =================

        // Internet to Edge Router & Public ALB
        edges.add(new TopologyEdgeDto("node-internet", "node-router", "Public IP", "WAN", "ACTIVE", "#64748b", false, 3));
        edges.add(new TopologyEdgeDto("node-internet", "node-aws-alb", "HTTPS :443", "WAN", "ACTIVE", "#64748b", false, 3));

        // Hybrid Site-to-Site IPSec VPN Tunnel (Router <===> AWS VGW)
        boolean vpnDown = "DISCONNECTED".equalsIgnoreCase(vpnStatus) || "DOWN".equalsIgnoreCase(vpnStatus);
        edges.add(new TopologyEdgeDto(
                "node-router",
                "node-aws-vgw",
                vpnDown ? "IPSec VPN: DOWN" : "IPSec VPN: ACTIVE (AES-256)",
                "VPN_TUNNEL",
                vpnDown ? "DOWN" : "ACTIVE",
                vpnDown ? "#ef4444" : "#10b981",
                true,
                vpnDown ? 2 : 4
        ));

        // On-Premise Core Backbone
        edges.add(new TopologyEdgeDto("node-router", "node-firewall", "Gig0/0 (1 Gbps)", "FIBER", "ACTIVE", "#3b82f6", false, 3));
        edges.add(new TopologyEdgeDto("node-firewall", "node-core-sw", "802.1Q Trunk", "FIBER", "ACTIVE", "#3b82f6", false, 3));

        // Core to Access Switches (Trunks)
        edges.add(new TopologyEdgeDto("node-core-sw", "node-acc-1", "Trunk (VLAN 10,20)", "TRUNK", "ACTIVE", "#0284c7", false, 2));
        edges.add(new TopologyEdgeDto("node-core-sw", "node-acc-2", "Trunk (VLAN 30)", "TRUNK", "ACTIVE", "#0284c7", false, 2));
        edges.add(new TopologyEdgeDto("node-core-sw", "node-acc-3", "Trunk (VLAN 40)", "TRUNK", "ACTIVE", "#0284c7", false, 2));

        // Access Switches to Endpoints
        edges.add(new TopologyEdgeDto("node-acc-1", "node-vlan-10", "FastEthernet", "ACCESS", "ACTIVE", "#94a3b8", false, 1));
        edges.add(new TopologyEdgeDto("node-acc-1", "node-vlan-20", "FastEthernet", "ACCESS", "ACTIVE", "#94a3b8", false, 1));
        edges.add(new TopologyEdgeDto("node-acc-2", "node-vlan-30", "FastEthernet", "ACCESS", "ACTIVE", "#94a3b8", false, 1));
        edges.add(new TopologyEdgeDto("node-acc-3", "node-vlan-40", "FastEthernet", "ACCESS", "ACTIVE", "#94a3b8", false, 1));

        // AWS Cloud VPC Interconnects
        edges.add(new TopologyEdgeDto("node-aws-vgw", "node-aws-ec2", "Internal VPC Route", "VPC_ROUTE", "ACTIVE", "#10b981", false, 2));
        edges.add(new TopologyEdgeDto("node-aws-alb", "node-aws-ec2", "HTTP :8080 Forward", "ALB_TARGET", "ACTIVE", "#f97316", false, 2));
        edges.add(new TopologyEdgeDto("node-aws-ec2", "node-aws-rds", "MySQL :3306 (Private)", "SQL_LINK", "ACTIVE", "#6366f1", false, 2));
        edges.add(new TopologyEdgeDto("node-aws-ec2", "node-aws-s3", "S3 Endpoint (IAM)", "IAM_ENDPOINT", "ACTIVE", "#14b8a6", false, 2));

        return new TopologyGraphDto(nodes, edges, vpnStatus);
    }

    private String getColorForStatus(String status) {
        if ("OFFLINE".equalsIgnoreCase(status) || "DOWN".equalsIgnoreCase(status)) return "#ef4444"; // Red
        if ("WARNING".equalsIgnoreCase(status) || "DEGRADED".equalsIgnoreCase(status)) return "#f59e0b"; // Yellow/Orange
        if ("MAINTENANCE".equalsIgnoreCase(status)) return "#8b5cf6"; // Purple
        return "#10b981"; // Green
    }
}
