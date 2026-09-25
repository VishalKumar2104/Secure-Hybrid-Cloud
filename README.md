# SERNMS - Secure Enterprise Resource and Network Management System
> **Subtitle:** *A Secure Hybrid Cloud-Based Enterprise Network and Resource Monitoring Platform*  
> **Academic Level:** 8th-Semester Major Engineering Capstone Project  
> **Domains:** AWS Cloud Computing + Cisco Enterprise Networking + Cybersecurity + Full-Stack Java Spring Boot

---

## 1. Executive Summary

Modern enterprise organizations operate hybrid environments spanning on-premises corporate offices (MDF, IDF switches, next-generation perimeter firewalls, campus VLANs) and public cloud infrastructures (AWS VPC, multi-AZ compute clusters, managed databases, encrypted object storage).

**SERNMS** solves the visibility and management challenge by providing a unified, secure web platform to:
- Connect on-premises Cisco networking hardware to an AWS Cloud VPC via an encrypted **IKEv2 / IPSec Site-to-Site VPN tunnel**.
- Manage enterprise network devices (Routers, Switches, Firewalls, Access Points, Servers) and IPAM across corporate VLANs (HR, IT, Finance, Admin).
- Render real-time **interactive network topologies** with dynamic status halos.
- Ingest live telemetry from **AWS CloudWatch** (EC2 CPU, Network I/O, RDS storage, S3 object metrics).
- Provide automated threshold alarming (CPU > 80% warning, CPU > 90% critical, device offline, VPN disconnect).
- Coordinate incident lifecycle management (`OPEN` $\rightarrow$ `ASSIGNED` $\rightarrow$ `INVESTIGATING` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED`).
- Maintain an immutable, tamper-proof **security audit trail** for compliance (ISO 27001 / SOC 2).

---

## 2. High-Level Hybrid Architecture

```
[ Campus Workstations ]          [ Campus Core ]                 [ WAN Gateway ]                 [ AWS VPC: 10.0.0.0/16 ]
VLAN 10: HR (10.10.10.0/24)  ──┐
VLAN 20: IT (10.10.20.0/24)  ──┼─► [ SW-CORE-01 ] ──► [ FW-CORE-01 ] ──► [ R-EDGE-01 ] ══════════════╗
VLAN 30: FIN (10.10.30.0/24) ──┤   (Catalyst 3650)   (Cisco ASA)        (Cisco 2911)                 ║
VLAN 40: ADM (10.10.40.0/24) ──┘                                                                     ║
                                                                                                     ║ IPSec Tunnel
                                                                                                     ║ (AES-256 / SHA-256)
                                                                                                     ▼
                                                                                      [ AWS Virtual Private Gateway ]
                                                                                                     │
                                                   ┌─────────────────────────────────────────────────┴────────────────────────┐
                                                   │                                                                          │
                                       [ Public Subnet 10.0.1.0/24 ]                              [ Private App Subnet 10.0.10.0/24 ]
                                        - Application Load Balancer                                - SERNMS Spring Boot Core (EC2)
                                        - Internet Gateway (IGW)                                   - Cloud Telemetry Poller Engine
                                                   │                                                                  │
                                                   └─────────────────────────────┬────────────────────────────────────┘
                                                                                 │
                                                                 [ Private DB Subnet 10.0.20.0/24 ]
                                                                  - Amazon RDS MySQL 8.0 Multi-AZ
                                                                  - Strictly Air-Gapped from Internet
```

---

## 3. Technology Stack

- **Backend:** Java 17 LTS, Spring Boot 3.3.4, Spring Web, Spring Data JPA, Spring Security 6.
- **Security & RBAC:** JJWT 0.12.5 (HMAC-SHA256), BCrypt Password Hashing, CORS, Method-Level Security.
- **AWS Cloud Integration:** AWS SDK for Java v2 (EC2, RDS, S3, CloudWatch, STS) with **Dual-Mode Engine** (Live AWS SDK or Zero-Cost Enterprise Simulation Provider).
- **Database:** MySQL 8.0 / H2 in-memory MySQL mode for rapid standalone evaluation.
- **Interactive Visualization:** `vis-network` (Topology node-edge canvas), `Chart.js` (Real-time telemetry).
- **API Documentation:** SpringDoc OpenAPI 3 / Swagger UI (`/swagger-ui.html`).
- **DevOps:** Docker, Docker Compose, GitHub Actions CI/CD pipeline.
- **Network Simulation:** Cisco Packet Tracer 8.x / GNS3 running configs.

---

## 4. Pre-Seeded Demonstration Accounts (Role-Based Access Control)

| Username | Password | Assigned Role | Access Permissions |
| :--- | :--- | :--- | :--- |
| **`admin`** | `Admin@123` | `ROLE_ADMIN` | **Full Access:** User accounts, audit logs, devices, cloud, topology, lab. |
| **`netadmin`** | `NetAdmin@123` | `ROLE_NETWORK_ADMIN` | **Network Scope:** Cisco devices, VLANs, topology, network incidents & alerts. |
| **`cloudadmin`**| `CloudAdmin@123`| `ROLE_CLOUD_ADMIN` | **Cloud Scope:** EC2, RDS, S3, CloudWatch metrics, cloud tickets. |
| **`employee`** | `Emp@123` | `ROLE_EMPLOYEE` | **Staff Scope:** View permitted devices, raise incident tickets, comment. |

---

## 5. How to Run Locally

### Option A: Turnkey 1-Command Startup (Recommended)
Prerequisites: JDK 17+ and Maven 3.8+ installed.

```bash
# 1. Clone or navigate to the project directory
cd "Secure Hybrid cloud"

# 2. Run Spring Boot (compiles backend and serves embedded UI directly)
mvn spring-boot:run -pl sernms-backend
```
- Open browser: **`http://localhost:8080`**
- Interactive Swagger UI: **`http://localhost:8080/swagger-ui.html`**
- H2 Web Console: **`http://localhost:8080/h2-console`** (JDBC URL: `jdbc:h2:mem:sernms_db`)

### Option B: Docker Compose Multi-Container Orchestration
Prerequisites: Docker Desktop installed.

```bash
cd docker
docker compose up --build
```
- Spawns isolated MySQL 8.0 container and SERNMS application container.
- Available at **`http://localhost:8080`**.

---

## 6. Controlled Failure Injection & Viva Demonstration Script

During your final-year project defense, follow this exact sequence to demonstrate technical depth:

1. **Step 1 - Authentication:** Open `http://localhost:8080/login.html`. Click the fast-fill button **"Super Admin"** and log in.
2. **Step 2 - Executive Dashboard:** Highlight live KPI counters (Network Devices, AWS Resources, Incidents, System Health 96.5%).
3. **Step 3 - Network Topology:** Click **"Network Topology"**. Demonstrate the hierarchical layout: Internet $\rightarrow$ Edge Router $\rightarrow$ Perimeter Firewall $\rightarrow$ Core Switch $\rightarrow$ Campus VLANs (10, 20, 30, 40) alongside the AWS Cloud VPC branch.
4. **Step 4 - Failure Injection Lab:** Click **"Failure Simulation Lab"** in the sidebar:
   - Click **"Simulate OFFLINE"** on **R-EDGE-01**.
   - Notice the toast alert: *"Critical alert triggered: Device R-EDGE-01 is unreachable"*.
   - Return to **Topology**: Observe that `R-EDGE-01` has turned **RED**.
   - Return to **Dashboard**: Notice the System Health gauge dropped and a Critical Alarm appeared.
5. **Step 5 - Incident Ticket Creation:** Click **"Raise Incident"** for `R-EDGE-01` with Priority `CRITICAL`. Assign to `netadmin`.
6. **Step 6 - Role Switch:** Logout and log in as **`netadmin`**. Show that User Management and Audit Logs are hidden due to RBAC. Open the assigned ticket, post an investigation note, and mark as `RESOLVED`.
7. **Step 7 - AWS Cloud Management:** Click **"AWS Cloud Resources"**. Review the EC2 instances, RDS Multi-AZ database, and S3 buckets. Explain the dual-mode AWS SDK integration.
8. **Step 8 - Cisco Packet Tracer & VPN:** Click **"Cisco & Hybrid Architecture"**. Review the Cisco IOS running configurations for `R-EDGE-01`, `SW-CORE-01`, and the exact IKEv2 IPSec tunnel commands to AWS VGW.
9. **Step 9 - Audit Compliance:** Log back in as `admin`. Open **"Security Audit Logs"** to demonstrate that every login, failure simulation, and ticket update was immutably recorded with IP and timestamp.
10. **Step 10 - Report Export:** Click **"Reports & Exports"** and download the CSV summaries.

---

## 7. Project Structure

```
Secure Hybrid cloud/
├── pom.xml                                   # Root aggregator POM
├── .env.example                              # Environment configuration template
├── README.md                                 # Master project documentation
├── sernms-backend/                           # Spring Boot 3 + Java 17 backend
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/sernms/
│       │   │   ├── SernmsApplication.java    # Bootstrap application class
│       │   │   ├── config/                   # Security, OpenAPI, CORS, DataInitializer
│       │   │   ├── controller/               # 12 REST API Controllers
│       │   │   ├── dto/                      # Data Transfer Objects
│       │   │   ├── entity/                   # 13 JPA Relational Entities
│       │   │   ├── repository/               # 13 Spring Data Repositories
│       │   │   ├── security/                 # JWT Provider, AuthFilter, UserDetails
│       │   │   └── service/                  # Business Logic & AWS Providers
│       │   └── resources/
│       │       ├── application.yml           # Dev & MySQL configuration profiles
│       │       └── static/                   # Production-grade Enterprise Web Dashboard
│       │           ├── index.html            # Main SPA dashboard shell
│       │           ├── login.html            # Enterprise authentication portal
│       │           ├── css/styles.css        # Enterprise design system
│       │           └── js/app.js             # Client SPA controller & vis-network graph
│       └── test/                             # JUnit 5 integration test suite
├── network-simulation/                       # Cisco Networking artifacts
│   ├── README.md                             # Packet Tracer lab instructions
│   ├── configs/                              # Cisco IOS startup-configs (Router, Core, Access, ASA)
│   └── vpn/                                  # IPSec Site-to-Site command guide
├── docker/                                   # Containerization
│   ├── Dockerfile                            # Multi-stage Alpine container build
│   └── docker-compose.yml                    # MySQL + Spring Boot orchestration
└── .github/workflows/                        # CI/CD
    └── ci.yml                                # Automated build, test, and container packaging
```

---

## 8. Authors & Specialization Division

- **AWS Cloud, DevOps & Full-Stack Core:** Cloud VPC, EC2, RDS, S3, IAM, CloudWatch, Docker, GitHub Actions, Spring Boot REST Engine, Enterprise Dashboard.
- **Enterprise Networking & Cybersecurity:** Campus Network Topology, Cisco IOS Configurations, VLAN Subnetting (802.1Q), Inter-VLAN Routing (SVIs), NAT Overload, Cisco ASA Perimeter Firewall, Site-to-Site IPSec VPN.
