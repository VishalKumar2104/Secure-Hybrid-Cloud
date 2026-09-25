# SERNMS Network Simulation & Cisco Lab Guide

This directory contains complete configuration artifacts, IP addressing tables, and testing procedures for the **Enterprise Networking & Hybrid Cloud** portion of the major project.

---

## 1. Campus IP Addressing Plan

- **Campus Supernet:** `10.10.0.0/16`
- **AWS VPC Supernet:** `10.0.0.0/16`

| Device / VLAN | IP Address / Prefix | Gateway | Role / Function |
| :--- | :--- | :--- | :--- |
| **R-EDGE-01 (Gi0/0)** | `203.0.113.1/30` | `203.0.113.2` | Public WAN Gateway Interface |
| **R-EDGE-01 (Gi0/1)** | `10.10.0.1/30` | - | Inter-Router link to Perimeter Firewall |
| **R-EDGE-01 (Tunnel0)** | `169.254.10.2/30` | `169.254.10.1` | Point-to-Point IPSec VPN Tunnel with AWS VGW |
| **FW-CORE-01 (Outside)** | `10.10.0.2/30` | `10.10.0.1` | Perimeter Firewall External Interface |
| **FW-CORE-01 (Inside)** | `10.10.0.3/30` | - | Link to Core L3 Switch |
| **SW-CORE-01 (Uplink)** | `10.10.0.3/30` | `10.10.0.2` | Core Switch Uplink to Firewall |
| **VLAN 10 (HR)** | `10.10.10.0/24` | `10.10.10.1` | Human Resources workstations |
| **VLAN 20 (IT)** | `10.10.20.0/24` | `10.10.20.1` | IT Operations & Systems terminals |
| **VLAN 30 (Finance)** | `10.10.30.0/24` | `10.10.30.1` | Finance terminals |
| **VLAN 40 (Admin)** | `10.10.40.0/24` | `10.10.40.1` | Network device out-of-band management |

---

## 2. Cisco Packet Tracer Lab Setup Instructions

1. Open Cisco Packet Tracer 8.0+.
2. Place the following devices on the workspace:
   - **Router:** 1x Cisco 2911 or 4331 (`R-EDGE-01`)
   - **Firewall:** 1x Cisco ASA 5506-X (`FW-CORE-01`)
   - **Core Switch:** 1x Cisco Catalyst 3650-24TS Multilayer Switch (`SW-CORE-01`)
   - **Access Switches:** 3x Cisco Catalyst 2960-X (`SW-ACC-01`, `SW-ACC-02`, `SW-ACC-03`)
   - **End User PCs:** 4x PCs connected to appropriate access switch ports.
3. Open the CLI for each device and copy-paste the corresponding configuration script from `configs/`:
   - `configs/R-EDGE-01.cfg`
   - `configs/SW-CORE-01.cfg`
   - `configs/SW-ACC-01.cfg`
   - `configs/ASA-FW-01.cfg`
4. Test Inter-VLAN routing by pinging from PC in VLAN 10 (`10.10.10.50`) to default gateway `10.10.10.1`.
5. Test Access Control Lists by verifying that HR PC (`10.10.10.50`) is blocked from reaching Admin Management interface `10.10.40.11`.
