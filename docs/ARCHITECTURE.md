# SERNMS Architecture & Technical Reference

## 1. Overview
The **Secure Enterprise Resource and Network Management System (SERNMS)** is an integrated observability and operations platform that unifies on-premise Cisco networking infrastructure with AWS cloud resources across an encrypted IPSec VPN gateway.

## 2. Layered Architecture

```
+-----------------------------------------------------------------------------------+
|                        Presentation Tier (Web UI SPA)                             |
|  - Semantic HTML5, CSS Variables, Bootstrap 5 components                          |
|  - Chart.js for Real-time CPU, RAM & Network I/O Telemetry Graphs                 |
|  - vis-network for Dynamic Hierarchical Network Topology with Status Halos        |
+-----------------------------------------+-----------------------------------------+
                                          | HTTPS / REST (JSON + JWT)
+-----------------------------------------v-----------------------------------------+
|                        Security & Identity Boundary                               |
|  - Spring Security 6 Filter Chain (Stateless SessionCreationPolicy)               |
|  - JwtAuthenticationFilter (HMAC-SHA256 signature validation via JJWT 0.12.x)    |
|  - Role-Based Access Control (@PreAuthorize): ADMIN, NETWORK_ADMIN, CLOUD_ADMIN,   |
|    EMPLOYEE                                                                       |
+-----------------------------------------+-----------------------------------------+
                                          |
+-----------------------------------------v-----------------------------------------+
|                        Application & Business Logic Layer                         |
|  - AuthService, UserService, DeviceService, VlanService, TopologyService         |
|  - AwsService (Dual Provider: Live AWS SDK v2 + High-Fidelity Simulation Provider)|
|  - IncidentService (Ticketing Lifecycle & SLA Tracking)                           |
|  - AlertService (Threshold Engine: CPU > 80%, Device Offline, VPN Severed)        |
|  - AuditLogService (Tamper-proof compliance recording with IP & user timestamps)  |
|  - ReportService (Executive Health Summaries & CSV exports)                       |
|  - DemoService (Controlled viva failure injection testbed)                        |
+-----------------------------------------+-----------------------------------------+
                                          | Spring Data JPA / Hibernate ORM
+-----------------------------------------v-----------------------------------------+
|                        Persistence Tier (Relational Datastore)                    |
|  - Multi-profile persistence: H2 in-memory MySQL mode for rapid evaluation        |
|  - Production Multi-AZ Amazon RDS MySQL 8.0 cluster in isolated private subnets   |
+-----------------------------------------------------------------------------------+
```

## 3. Database Normalization & Entities
The database schema is fully normalized into 3NF:
- `users`: Core authentication identity, BCrypt hashed passwords, account status.
- `roles` & `user_roles`: Multi-role assignment linking users to permissions.
- `vlans`: 802.1Q campus subnets (HR, IT, Finance, Admin).
- `network_devices`: Inventory with MAC address, IP address, manufacturer, and status.
- `network_interfaces`: Physical/virtual interfaces associated with devices.
- `network_links`: Physical cabling and logical VPN tunnels between interfaces.
- `aws_resources`: Cloud assets (EC2, RDS, S3, ALB, VGW) with instance types and regions.
- `aws_metrics`: Historical telemetry points for time-series analytics.
- `incidents` & `incident_comments`: Ticket lifecycle with priority, status, and engineer notes.
- `alerts`: Active alarms with severity levels (`INFO`, `WARNING`, `CRITICAL`).
- `audit_logs`: Append-only compliance log of all system actions.
- `notifications`: User and broadcast alert notifications.

## 4. Security Architecture
- **Stateless Authentication:** Every request is authenticated through standard Bearer JWT tokens.
- **Role Isolation:**
  - `ROLE_ADMIN`: Master access to user provisioning, role grants, audit inspection, and device controls.
  - `ROLE_NETWORK_ADMIN`: Restricted to network devices, VLANs, and network incident resolution.
  - `ROLE_CLOUD_ADMIN`: Restricted to EC2/RDS start/stop actions and CloudWatch metrics.
  - `ROLE_EMPLOYEE`: Restricted to ticket submission and viewing assigned items.
- **Audit Logging:** Every administrative action triggers an automated audit record containing the username, action, target entity, client IP address, timestamp, and status.
