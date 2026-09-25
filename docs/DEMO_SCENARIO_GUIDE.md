# SERNMS Major Project Viva Defense & Demo Guide

This script is structured to showcase both team members' technical specializations during the final-year viva defense.

---

## 1. Demo Credentials Quick Reference

| Role | Username | Password | Demonstrated Specialization |
| :--- | :--- | :--- | :--- |
| **Super Admin** | `admin` | `Admin@123` | System oversight, security compliance, audit logs |
| **Network Admin** | `netadmin` | `NetAdmin@123` | Cisco networking, VLANs, switching, routing, Packet Tracer |
| **Cloud Admin** | `cloudadmin` | `CloudAdmin@123` | AWS Cloud VPC, EC2, RDS, CloudWatch, telemetry |
| **Staff Employee** | `employee` | `Emp@123` | End-user ticketing workflow |

---

## 2. 10-Step Presentation Script

### Phase 1: Authentication & RBAC (Presented by Cloud Dev)
1. Open `http://localhost:8080/login.html`.
2. Click the fast-fill button **"Super Admin"** (`admin / Admin@123`).
3. Click **"Authenticate & Access Console"**.
4. Point out the JWT token generated in localStorage with HMAC-SHA256 signature and role claims.

### Phase 2: Executive Dashboard & Health KPI (Presented by Cloud Dev)
1. On `index.html`, explain the 5 top metric cards:
   - Total Network Devices (8 devices across MDF and IDFs).
   - AWS Cloud Resources (7 cloud resources across EC2, RDS, S3, VGW).
   - Open Incidents and Critical Alarms.
   - Overall System Health Gauge (calculating SLA compliance).
2. Point out the **Hybrid IPSec VPN Status Chip** (`IPSec VPN: CONNECTED`).
3. Show the **Chart.js CPU Utilization** and **Infrastructure Distribution** graphs.

### Phase 3: Interactive Network Topology (Presented by Network Dev)
1. Click **"Network Topology"** in the sidebar.
2. Demonstrate the hierarchical layout:
   - Tier 1: Public Internet & External WAN
   - Tier 2: Enterprise Edge Router (`R-EDGE-01`) $\longleftrightarrow$ AWS Virtual Private Gateway (`VGW-01`) via green dashed IPSec tunnel.
   - Tier 3: NextGen Perimeter Firewall (`FW-CORE-01`)
   - Tier 4: Core Layer 3 Switch (`SW-CORE-01`) with SVIs for Inter-VLAN routing.
   - Tier 5: Access Layer Switches (`SW-ACC-01`, `SW-ACC-02`, `SW-ACC-03`).
   - Tier 6: Enterprise VLAN Endpoints: VLAN 10 (HR), VLAN 20 (IT), VLAN 30 (Finance), VLAN 40 (Admin).
3. Click any node to open the **Inspected Node Telemetry** inspection drawer.

### Phase 4: Network Device Inventory & IPAM (Presented by Network Dev)
1. Click **"Network Devices"**.
2. Filter by status (`ONLINE` / `OFFLINE`) or search by name.
3. Click the **"Ping"** button on `R-EDGE-01` to show live ICMP echo reply verification.
4. Show how physical interfaces (GigabitEthernet0/1, FastEthernet) are mapped to each device.

### Phase 5: Controlled Failure Injection Lab (Presented by Both)
1. Click **"Failure Simulation Lab"** in the sidebar.
2. Under Scenario 1, click **"Simulate OFFLINE"** on `R-EDGE-01`.
3. Notice:
   - Instant toast notification: *"Simulated failure: Device R-EDGE-01 is now OFFLINE. Critical alert triggered."*
   - Active alarm count increments immediately.
4. Return to **"Network Topology"**:
   - `R-EDGE-01` node has turned **RED** to indicate an unreachable device.
5. Return to **"Executive Dashboard"**:
   - Critical Alarms counter updated, and System Health percentage drops.

### Phase 6: Incident Escalation & Resolution (Presented by Network Dev)
1. Click **"Incident Tickets"**.
2. Click **"+ Raise Incident"**:
   - Title: `R-EDGE-01 hardware failure in MDF Rack 01`
   - Category: `NETWORK`
   - Priority: `CRITICAL`
   - Description: `Edge router unresponsive to ICMP ping. IPSec tunnel failing over.`
3. Logout and login as **`netadmin / NetAdmin@123`**:
   - Notice: **Security Audit Logs** and **User Accounts** are hidden in the sidebar (enforcing strict RBAC).
4. Open the incident ticket, click **"Inspect"**:
   - Add a comment: *"Investigated power supply unit on MDF Rack 01. Switched to redundant UPS rail."*
   - Change Status to `RESOLVED` and click **"Update Status"**.

### Phase 7: AWS Cloud Infrastructure Management (Presented by Cloud Dev)
1. Login as **`cloudadmin / CloudAdmin@123`** or `admin`.
2. Click **"AWS Cloud Resources"**:
   - Show EC2 instances (`SERNMS-App-Server-01`, `SERNMS-Worker-Node-02`).
   - Show Amazon RDS Multi-AZ MySQL database (`rds-mysql-prod-01`).
   - Show Amazon S3 compliance buckets (`s3-sernms-audit-reports`).
3. Explain the **Dual-Mode AWS Architecture**:
   - Production Mode: Queries real AWS CloudWatch APIs via AWS SDK for Java v2.
   - Simulation Mode: Enables zero-cost offline demonstration without any AWS cloud bills.

### Phase 8: Cisco Packet Tracer & IPSec Configuration (Presented by Network Dev)
1. Click **"Cisco & Hybrid Architecture"**.
2. Review the live Cisco IOS running configurations:
   - `R-EDGE-01` (NAT overload and IPSec tunnel).
   - `SW-CORE-01` (Inter-VLAN routing, SVIs, Access Control Lists).
   - `FW-CORE-01` (ASA stateful inspection).
   - Show the IKEv2 proposal, policy, and crypto map commands that connect to AWS VGW.

### Phase 9: Security Audit & Compliance Verification (Presented by Both)
1. Login as `admin`.
2. Click **"Security Audit Logs"**.
3. Point out the immutable audit trail:
   - Records every event: `USER_LOGIN`, `DEVICE_STATUS_CHANGED`, `INCIDENT_CREATED`, `INCIDENT_STATUS_CHANGED`, `DEMO_SCENARIO_EXECUTED`.
   - Each entry captures the username, client IP address, action, and timestamp.

### Phase 10: Reports Export & Docker/CI-CD (Presented by Both)
1. Click **"Reports & Exports"**.
2. Download CSV exports for Network Devices, AWS Resources, Incidents, and Audit Logs.
3. Show the `docker-compose.yml` file and GitHub Actions `ci.yml` pipeline.
