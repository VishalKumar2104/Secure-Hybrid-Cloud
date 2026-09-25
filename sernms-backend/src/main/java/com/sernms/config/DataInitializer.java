package com.sernms.config;

import com.sernms.entity.*;
import com.sernms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final VlanRepository vlanRepository;
    private final NetworkDeviceRepository deviceRepository;
    private final NetworkInterfaceRepository interfaceRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           VlanRepository vlanRepository,
                           NetworkDeviceRepository deviceRepository,
                           NetworkInterfaceRepository interfaceRepository,
                           IncidentRepository incidentRepository,
                           AlertRepository alertRepository,
                           AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.vlanRepository = vlanRepository;
        this.deviceRepository = deviceRepository;
        this.interfaceRepository = interfaceRepository;
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking SERNMS database initialization...");
        seedRoles();
        seedUsers();
        seedVlans();
        seedNetworkDevices();
        seedIncidentsAndAlerts();
        log.info("SERNMS database initialization completed successfully.");
    }

    private void seedRoles() {
        createRoleIfNotFound("ROLE_ADMIN", "Full system administrator access across all cloud and network modules");
        createRoleIfNotFound("ROLE_NETWORK_ADMIN", "Network infrastructure, device monitoring, and topology management");
        createRoleIfNotFound("ROLE_CLOUD_ADMIN", "AWS cloud resources, EC2/RDS monitoring, and cloud operations");
        createRoleIfNotFound("ROLE_EMPLOYEE", "Standard employee view permitted resources and raise incident tickets");
    }

    private void createRoleIfNotFound(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(new Role(name, description));
            log.info("Seeded role: {}", name);
        }
    }

    private void seedUsers() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role netAdminRole = roleRepository.findByName("ROLE_NETWORK_ADMIN").orElseThrow();
        Role cloudAdminRole = roleRepository.findByName("ROLE_CLOUD_ADMIN").orElseThrow();
        Role empRole = roleRepository.findByName("ROLE_EMPLOYEE").orElseThrow();

        // 1. Super Admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@sernms.enterprise.local", passwordEncoder.encode("Admin@123"), "System", "Administrator", "IT Operations");
            admin.setRoles(new HashSet<>(Set.of(adminRole, netAdminRole, cloudAdminRole)));
            userRepository.save(admin);
            log.info("Seeded user: admin (Admin@123)");
        }

        // 2. Network Admin (for your friend's specialization)
        if (!userRepository.existsByUsername("netadmin")) {
            User netAdmin = new User("netadmin", "netadmin@sernms.enterprise.local", passwordEncoder.encode("NetAdmin@123"), "Cisco", "NetworkEngineer", "Network Infrastructure");
            netAdmin.setRoles(new HashSet<>(Set.of(netAdminRole)));
            userRepository.save(netAdmin);
            log.info("Seeded user: netadmin (NetAdmin@123)");
        }

        // 3. Cloud Admin (for your AWS cloud specialization)
        if (!userRepository.existsByUsername("cloudadmin")) {
            User cloudAdmin = new User("cloudadmin", "cloudadmin@sernms.enterprise.local", passwordEncoder.encode("CloudAdmin@123"), "Cloud", "Architect", "Cloud DevOps");
            cloudAdmin.setRoles(new HashSet<>(Set.of(cloudAdminRole)));
            userRepository.save(cloudAdmin);
            log.info("Seeded user: cloudadmin (CloudAdmin@123)");
        }

        // 4. Employee
        if (!userRepository.existsByUsername("employee")) {
            User employee = new User("employee", "staff@sernms.enterprise.local", passwordEncoder.encode("Emp@123"), "John", "Doe", "Human Resources");
            employee.setRoles(new HashSet<>(Set.of(empRole)));
            userRepository.save(employee);
            log.info("Seeded user: employee (Emp@123)");
        }
    }

    private void seedVlans() {
        if (vlanRepository.count() == 0) {
            vlanRepository.save(new Vlan(10, "HR-Network", "10.10.10.0/24", "10.10.10.1", "Human Resources Department Subnet"));
            vlanRepository.save(new Vlan(20, "IT-Ops-Network", "10.10.20.0/24", "10.10.20.1", "IT Operations & Engineering Workstations"));
            vlanRepository.save(new Vlan(30, "Finance-Network", "10.10.30.0/24", "10.10.30.1", "Accounting & Finance Terminals"));
            vlanRepository.save(new Vlan(40, "Admin-Mgmt", "10.10.40.0/24", "10.10.40.1", "Network Out-of-Band Management Subnet"));
            log.info("Seeded 4 enterprise VLAN subnets (VLAN 10, 20, 30, 40)");
        }
    }

    private void seedNetworkDevices() {
        if (deviceRepository.count() == 0) {
            Vlan vlan10 = vlanRepository.findByVlanId(10).orElse(null);
            Vlan vlan20 = vlanRepository.findByVlanId(20).orElse(null);
            Vlan vlan30 = vlanRepository.findByVlanId(30).orElse(null);
            Vlan vlan40 = vlanRepository.findByVlanId(40).orElse(null);

            // 1. Edge Router
            createDeviceWithInterface("R-EDGE-01", "ROUTER", "10.10.0.1", "00:1A:A1:2B:3C:01",
                    "Head Office - MDF Rack 01", "Cisco", "ISR 4331 / 2911", "ONLINE", vlan40,
                    "Enterprise Edge Router with NAT Overload & IPSec VPN Gateway to AWS VPC");

            // 2. NextGen Firewall
            createDeviceWithInterface("FW-CORE-01", "FIREWALL", "10.10.0.2", "00:1A:A1:2B:3C:02",
                    "Head Office - MDF Rack 01", "Cisco", "ASA 5506-X", "ONLINE", vlan40,
                    "Enterprise Perimeter Firewall with Deep Packet Inspection & Threat Prevention");

            // 3. Core L3 Switch
            createDeviceWithInterface("SW-CORE-01", "SWITCH", "10.10.0.3", "00:1A:A1:2B:3C:03",
                    "Head Office - MDF Rack 02", "Cisco", "Catalyst 3650-24TS", "ONLINE", vlan40,
                    "Distribution & Core Layer 3 Switch with SVIs and Inter-VLAN Routing");

            // 4. Access Switch 1 (HR & IT)
            createDeviceWithInterface("SW-ACC-01", "SWITCH", "10.10.0.11", "00:1A:A1:2B:3C:11",
                    "Building A - IDF 1", "Cisco", "Catalyst 2960-X", "ONLINE", vlan10,
                    "Access layer switch serving HR and IT operations endpoints");

            // 5. Access Switch 2 (Finance)
            createDeviceWithInterface("SW-ACC-02", "SWITCH", "10.10.0.12", "00:1A:A1:2B:3C:12",
                    "Building A - IDF 2", "Cisco", "Catalyst 2960-X", "ONLINE", vlan30,
                    "Access layer switch serving Finance department secure hosts");

            // 6. Access Switch 3 (Admin)
            createDeviceWithInterface("SW-ACC-03", "SWITCH", "10.10.0.13", "00:1A:A1:2B:3C:13",
                    "Building B - IDF 1", "Cisco", "Catalyst 2960-X", "ONLINE", vlan40,
                    "Out-of-band management switch for IT server room equipment");

            // 7. Domain Controller Server
            createDeviceWithInterface("SRV-DC-01", "SERVER", "10.10.40.10", "00:1A:A1:2B:3C:50",
                    "Data Center - Server Rack A", "Dell", "PowerEdge R640", "ONLINE", vlan40,
                    "On-premise Active Directory Domain Controller and Enterprise DNS server");

            // 8. Wireless Access Point
            createDeviceWithInterface("AP-FLOOR-01", "ACCESS_POINT", "10.10.20.25", "00:1A:A1:2B:3C:77",
                    "Building A - 2nd Floor Ceiling", "Cisco", "Aironet 2800", "ONLINE", vlan20,
                    "Enterprise Wi-Fi 6 Access Point bridging 802.11ax wireless clients to VLAN 20");

            log.info("Seeded 8 enterprise network devices with physical interfaces");
        }
    }

    private void createDeviceWithInterface(String name, String type, String ip, String mac,
                                           String location, String mfr, String model, String status,
                                           Vlan vlan, String description) {
        NetworkDevice device = new NetworkDevice(name, type, ip, mac, location, mfr, model, status, vlan, description);
        NetworkDevice saved = deviceRepository.save(device);

        NetworkInterface if1 = new NetworkInterface(saved, "GigabitEthernet0/1", ip, mac, "1 Gbps", "UP");
        interfaceRepository.save(if1);
    }

    private void seedIncidentsAndAlerts() {
        if (incidentRepository.count() == 0) {
            User reporter = userRepository.findByUsername("admin").orElseThrow();
            User netAdmin = userRepository.findByUsername("netadmin").orElse(null);

            Incident inc1 = new Incident(
                    "INC-202609-001",
                    "Intermittent latency observed on Access Switch SW-ACC-02",
                    "Finance department reporting periodic connection slowdowns during batch report generation. Ping latency spiked to 240ms.",
                    "NETWORK",
                    "HIGH",
                    reporter,
                    "NETWORK_DEVICE",
                    "SW-ACC-02"
            );
            inc1.setStatus("INVESTIGATING");
            inc1.setAssignee(netAdmin);
            incidentRepository.save(inc1);

            Incident inc2 = new Incident(
                    "INC-202609-002",
                    "CloudWatch Warning: EC2 App Server Memory Threshold",
                    "SERNMS-App-Server-01 memory utilization reached 78% during peak concurrent telemetry ingestion.",
                    "CLOUD",
                    "MEDIUM",
                    reporter,
                    "EC2_INSTANCE",
                    "i-07f9c81a2b"
            );
            inc2.setStatus("OPEN");
            incidentRepository.save(inc2);

            log.info("Seeded sample incidents");
        }

        if (alertRepository.count() == 0) {
            alertRepository.save(new Alert(
                    "IPSec Site-to-Site VPN Tunnel Active",
                    "Encrypted IKEv2 IPSec tunnel between Cisco 2911 (10.10.0.1) and AWS VGW (10.0.0.0/16) established with AES-256-GCM encryption.",
                    "INFO",
                    "SECURITY",
                    "VPN",
                    "vgw-sernms-hybrid-01"
            ));

            alertRepository.save(new Alert(
                    "High Latency Warning on SW-ACC-02",
                    "SNMP poller detected response latency > 180ms on switch SW-ACC-02 in Building A IDF 2.",
                    "WARNING",
                    "NETWORK",
                    "SWITCH",
                    "SW-ACC-02"
            ));
            log.info("Seeded sample alerts");
        }

        if (auditLogRepository.count() == 0) {
            auditLogRepository.save(new AuditLog(
                    "SYSTEM",
                    "PLATFORM_BOOTSTRAP",
                    "SERNMS",
                    "1.0.0",
                    "127.0.0.1",
                    "SUCCESS",
                    "Initialized SERNMS Enterprise Security, Hybrid Cloud VPC, and Campus Network Models"
            ));
        }
    }
}
