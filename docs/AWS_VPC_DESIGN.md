# SERNMS AWS VPC Architecture & Security Design

## 1. VPC CIDR Allocation (10.0.0.0/16)
The AWS Cloud environment is provisioned across two Availability Zones (`us-east-1a` and `us-east-1b`) for high availability:

| Subnet Identifier | CIDR Prefix | Availability Zone | Routing & Gateway | Typical Resources |
| :--- | :--- | :--- | :--- | :--- |
| **Public Subnet 1** | `10.0.1.0/24` | `us-east-1a` | Route to Internet Gateway (`igw`) | Public ALB, NAT Gateway AZ-1 |
| **Public Subnet 2** | `10.0.2.0/24` | `us-east-1b` | Route to Internet Gateway (`igw`) | Public ALB, Bastion Jump Host |
| **Private App Subnet 1** | `10.0.10.0/24`| `us-east-1a` | Outbound via NAT Gateway AZ-1 | SERNMS Spring Boot Core (EC2) |
| **Private App Subnet 2** | `10.0.11.0/24`| `us-east-1b` | Outbound via NAT Gateway AZ-1 | Worker Nodes |
| **Private DB Subnet 1** | `10.0.20.0/24`| `us-east-1a` | Local VPC route only | Amazon RDS MySQL (Primary) |
| **Private DB Subnet 2** | `10.0.21.0/24`| `us-east-1b` | Local VPC route only | Amazon RDS MySQL (Standby AZ) |

---

## 2. Route Table Architecture

1. **Public Route Table:**
   - `0.0.0.0/0` $\rightarrow$ `igw-sernms` (Internet Gateway)
   - `10.10.0.0/16` $\rightarrow$ `vgw-sernms` (Virtual Private Gateway for on-premises traffic)
2. **Private App Route Table:**
   - `0.0.0.0/0` $\rightarrow$ `nat-sernms-az1` (NAT Gateway for outbound package updates)
   - `10.10.0.0/16` $\rightarrow$ `vgw-sernms` (Direct hybrid access via IPSec VPN)
3. **Private Database Route Table:**
   - Local VPC route only (`10.0.0.0/16` $\rightarrow$ `local`). Completely isolated from internet gateways and NAT.

---

## 3. Security Groups Matrix (Principle of Least Privilege)

```
[Internet Users]
      │
      ▼ HTTPS (443)
[sg-sernms-alb]
      │
      ▼ HTTP (8080) - Source restricted to sg-sernms-alb only
[sg-sernms-app]
      │
      ▼ MySQL (3306) - Source restricted to sg-sernms-app only
[sg-sernms-db]
```

- **`sg-sernms-alb`:**
  - Inbound: Port 443 (HTTPS) from `0.0.0.0/0`.
  - Outbound: Port 8080 to `sg-sernms-app`.
- **`sg-sernms-app`:**
  - Inbound: Port 8080 from `sg-sernms-alb`.
  - Inbound: Port 22 (SSH) and ICMP Ping from on-premises Admin Subnet (`10.10.40.0/24`) via VPN tunnel.
  - Outbound: Port 3306 to `sg-sernms-db`.
  - Outbound: Port 443 (HTTPS) via NAT Gateway to AWS API endpoints.
- **`sg-sernms-db`:**
  - Inbound: Port 3306 (MySQL) strictly from `sg-sernms-app`.
  - Outbound: None (closed).

---

## 4. Virtual Private Gateway & IPSec VPN Tunnel
- **AWS Service:** Virtual Private Gateway (VGW) attached to VPC `10.0.0.0/16`.
- **Peer Gateway:** On-premise Cisco Router `R-EDGE-01` at public IP `203.0.113.1`.
- **Encryption:** IKEv2 Phase 1 (AES-256-CBC, SHA-256, DH Group 14), IPSec Phase 2 (ESP-AES-256, ESP-SHA-256).
- **Route Propagation:** Enabled on private route tables so campus traffic (`10.10.0.0/16`) automatically routes over the VPN tunnel.
