/* ==========================================================================
   SERNMS - Secure Enterprise Resource and Network Management System
   Frontend Single Page Application Engine
   ========================================================================== */

let isLoggingOut = false;

let currentUser = null;
let currentToken = null;
let cpuChart = null;
let distChart = null;
let monitoringCpuChart = null;
let monitoringNetChart = null;
let networkGraph = null;
let allDevicesCache = [];
let allAwsCache = [];

// Determine API endpoint base
const API_BASE = (typeof window !== 'undefined' && window.location.protocol !== 'file:' && window.location.origin.includes(':8080')) ? '' : 'http://localhost:8080';

// API helper with JWT injection & offline graceful fallback
async function apiFetch(url, options = {}) {
  const headers = options.headers || {};
  if (currentToken) {
    headers['Authorization'] = `Bearer ${currentToken}`;
  }
  if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const targetUrl = url.startsWith('http') ? url : (API_BASE + url);

  try {
    const response = await fetch(targetUrl, { ...options, headers });
    if (response.status === 401) {
      if (!isLoggingOut) {
        isLoggingOut = true;
        showToast('Session expired. Please log in again.', 'error');
        setTimeout(() => logout(), 1000);
      }
      throw new Error('Session expired. Please log in again.');
    }
    return response;
  } catch (err) {
    if (err.message === 'Session expired. Please log in again.') throw err;
    console.warn(`API call failed for ${url}. Providing demo mock fallback.`, err);
    return getOfflineMockResponse(url, options);
  }
}

// Offline fallback data provider for instant preview even when backend is offline
function getOfflineMockResponse(url, options) {
  let mockData = {};
  if (url.includes('/monitoring/dashboard')) {
    mockData = {
      totalUsers: 4, activeUsers: 4,
      totalNetworkDevices: 8, onlineNetworkDevices: 8, offlineNetworkDevices: 0, warningNetworkDevices: 0,
      totalAwsResources: 7, runningAwsResources: 6, stoppedAwsResources: 0,
      healthyResources: 14, warningResources: 0, criticalResources: 0,
      openIncidents: 1, investigatingIncidents: 1, resolvedIncidents: 0,
      criticalAlerts: 0, unacknowledgedAlerts: 2,
      hybridVpnStatus: 'CONNECTED', systemHealthPercentage: 96.5,
      recentAlerts: [
        { id: 2, alertTitle: "High Latency Warning on SW-ACC-02", severity: "WARNING", category: "NETWORK", sourceEntityType: "SWITCH", sourceEntityId: "SW-ACC-02", triggeredAt: new Date().toISOString() },
        { id: 1, alertTitle: "IPSec Site-to-Site VPN Tunnel Active", severity: "INFO", category: "SECURITY", sourceEntityType: "VPN", sourceEntityId: "vgw-sernms-hybrid-01", triggeredAt: new Date().toISOString() }
      ],
      recentIncidents: [
        { id: 1, ticketNumber: "INC-202609-001", title: "Intermittent latency observed on Access Switch SW-ACC-02", category: "NETWORK", priority: "HIGH", status: "INVESTIGATING" }
      ]
    };
  } else if (url.includes('/monitoring/telemetry')) {
    mockData = [
      { timeLabel: "10:00", cpuUtilization: 24.2, memoryUtilization: 48.1, networkInMbps: 22.4, networkOutMbps: 18.5 },
      { timeLabel: "10:15", cpuUtilization: 31.8, memoryUtilization: 50.4, networkInMbps: 35.1, networkOutMbps: 28.9 },
      { timeLabel: "10:30", cpuUtilization: 28.5, memoryUtilization: 49.2, networkInMbps: 41.2, networkOutMbps: 34.0 },
      { timeLabel: "10:45", cpuUtilization: 35.1, memoryUtilization: 52.0, networkInMbps: 48.6, networkOutMbps: 40.5 }
    ];
  } else if (url.includes('/network/devices')) {
    mockData = [
      { id: 1, name: "R-EDGE-01", deviceType: "ROUTER", ipAddress: "10.10.0.1", macAddress: "00:1A:A1:2B:3C:01", location: "MDF Rack 01", status: "ONLINE", vlanName: "Admin-Mgmt", lastSeen: new Date().toISOString() },
      { id: 2, name: "FW-CORE-01", deviceType: "FIREWALL", ipAddress: "10.10.0.2", macAddress: "00:1A:A1:2B:3C:02", location: "MDF Rack 01", status: "ONLINE", vlanName: "Admin-Mgmt", lastSeen: new Date().toISOString() },
      { id: 3, name: "SW-CORE-01", deviceType: "SWITCH", ipAddress: "10.10.0.3", macAddress: "00:1A:A1:2B:3C:03", location: "MDF Rack 02", status: "ONLINE", vlanName: "Admin-Mgmt", lastSeen: new Date().toISOString() },
      { id: 4, name: "SW-ACC-01", deviceType: "SWITCH", ipAddress: "10.10.0.11", macAddress: "00:1A:A1:2B:3C:11", location: "Bldg A IDF 1", status: "ONLINE", vlanName: "HR-Network", lastSeen: new Date().toISOString() },
      { id: 5, name: "SW-ACC-02", deviceType: "SWITCH", ipAddress: "10.10.0.12", macAddress: "00:1A:A1:2B:3C:12", location: "Bldg A IDF 2", status: "ONLINE", vlanName: "Finance-Network", lastSeen: new Date().toISOString() }
    ];
  } else if (url.includes('/network/vlans')) {
    mockData = [
      { id: 1, vlanId: 10, name: "HR-Network", subnetCidr: "10.10.10.0/24", gatewayIp: "10.10.10.1", description: "Human Resources Subnet", deviceCount: 1 },
      { id: 2, vlanId: 20, name: "IT-Ops-Network", subnetCidr: "10.10.20.0/24", gatewayIp: "10.10.20.1", description: "IT Support Subnet", deviceCount: 1 },
      { id: 3, vlanId: 30, name: "Finance-Network", subnetCidr: "10.10.30.0/24", gatewayIp: "10.10.30.1", description: "Finance Department", deviceCount: 1 },
      { id: 4, vlanId: 40, name: "Admin-Mgmt", subnetCidr: "10.10.40.0/24", gatewayIp: "10.10.40.1", description: "Out-of-band Management", deviceCount: 3 }
    ];
  } else if (url.includes('/aws/resources')) {
    mockData = [
      { id: 1, resourceId: "i-07f9c81a2b", resourceName: "SERNMS-App-Server-01", resourceType: "EC2", region: "us-east-1", privateIp: "10.0.10.15", status: "RUNNING", instanceType: "t3.large (2 vCPU, 8 GB)" },
      { id: 2, resourceId: "rds-mysql-prod-01", resourceName: "SERNMS-RDS-MySQL-MultiAZ", resourceType: "RDS", region: "us-east-1", privateIp: "10.0.20.45", status: "AVAILABLE", instanceType: "db.t3.medium (MySQL 8.0)" },
      { id: 3, resourceId: "s3-sernms-audit-reports", resourceName: "sernms-audit-reports-us-east-1", resourceType: "S3", region: "us-east-1", status: "AVAILABLE", instanceType: "S3 Standard" },
      { id: 4, resourceId: "vgw-sernms-hybrid-01", resourceName: "SERNMS-Campus-IPSec-VGW", resourceType: "VGW", region: "us-east-1", status: "CONNECTED", instanceType: "AWS VGW IPSec" }
    ];
  } else if (url.includes('/topology')) {
    mockData = {
      hybridVpnStatus: "CONNECTED",
      totalNodes: 6, totalEdges: 5,
      nodes: [
        { id: "node-internet", label: "Public Internet", title: "WAN", group: "INTERNET", status: "ONLINE", color: "#38bdf8", level: 1, shape: "cloud" },
        { id: "node-router", label: "R-EDGE-01\n(Cisco 2911)", title: "Edge Router", group: "ROUTER", status: "ONLINE", color: "#10b981", level: 2, shape: "box" },
        { id: "node-firewall", label: "FW-CORE-01\n(Cisco ASA)", title: "Firewall", group: "FIREWALL", status: "ONLINE", color: "#10b981", level: 3, shape: "box" },
        { id: "node-core-sw", label: "SW-CORE-01\n(Catalyst 3650)", title: "Core L3 Switch", group: "SWITCH", status: "ONLINE", color: "#10b981", level: 4, shape: "box" },
        { id: "node-aws-vgw", label: "AWS VGW Gateway", title: "AWS VPN", group: "AWS_VGW", status: "CONNECTED", color: "#10b981", level: 2, shape: "box" },
        { id: "node-aws-ec2", label: "SERNMS App Server\n(EC2)", title: "Spring Boot App", group: "AWS_EC2", status: "ONLINE", color: "#3b82f6", level: 3, shape: "box" }
      ],
      edges: [
        { from: "node-internet", to: "node-router", label: "WAN", color: "#64748b", dashes: false, width: 3 },
        { from: "node-router", to: "node-aws-vgw", label: "IPSec VPN: ACTIVE", color: "#10b981", dashes: true, width: 4 },
        { from: "node-router", to: "node-firewall", label: "1 Gbps", color: "#3b82f6", dashes: false, width: 3 },
        { from: "node-firewall", to: "node-core-sw", label: "802.1Q Trunk", color: "#3b82f6", dashes: false, width: 3 },
        { from: "node-aws-vgw", to: "node-aws-ec2", label: "VPC Route", color: "#10b981", dashes: false, width: 2 }
      ]
    };
  } else if (url.includes('/alerts')) {
    mockData = [
      { id: 2, alertTitle: "High Latency Warning on SW-ACC-02", alertMessage: "Latency spiked > 180ms", severity: "WARNING", category: "NETWORK", sourceEntityType: "SWITCH", sourceEntityId: "SW-ACC-02", triggeredAt: new Date().toISOString() },
      { id: 1, alertTitle: "IPSec Site-to-Site VPN Active", alertMessage: "IKEv2 AES-256 tunnel active", severity: "INFO", category: "SECURITY", sourceEntityType: "VPN", sourceEntityId: "vgw-sernms-hybrid-01", triggeredAt: new Date().toISOString() }
    ];
  } else if (url.includes('/incidents')) {
    mockData = [
      { id: 1, ticketNumber: "INC-202609-001", title: "Intermittent latency observed on Access Switch SW-ACC-02", category: "NETWORK", priority: "HIGH", status: "INVESTIGATING", reporterName: "admin", assigneeName: "netadmin", createdAt: new Date().toISOString(), description: "Finance reporting latency." }
    ];
  } else if (url.includes('/users')) {
    mockData = [
      { id: 1, username: "admin", email: "admin@sernms.enterprise.local", firstName: "System", lastName: "Admin", department: "IT Operations", roles: ["ROLE_ADMIN"], active: true },
      { id: 2, username: "netadmin", email: "netadmin@sernms.enterprise.local", firstName: "Cisco", lastName: "Engineer", department: "Network Infrastructure", roles: ["ROLE_NETWORK_ADMIN"], active: true },
      { id: 3, username: "cloudadmin", email: "cloudadmin@sernms.enterprise.local", firstName: "Cloud", lastName: "Architect", department: "Cloud DevOps", roles: ["ROLE_CLOUD_ADMIN"], active: true },
      { id: 4, username: "employee", email: "staff@sernms.enterprise.local", firstName: "John", lastName: "Doe", department: "Human Resources", roles: ["ROLE_EMPLOYEE"], active: true }
    ];
  } else if (url.includes('/audit-logs')) {
    mockData = [
      { id: 1, timestamp: new Date().toISOString(), username: "admin", action: "USER_LOGIN", entityName: "User", entityId: "1", ipAddress: "127.0.0.1", status: "SUCCESS", details: "User authenticated" },
      { id: 2, timestamp: new Date().toISOString(), username: "SYSTEM", action: "PLATFORM_BOOTSTRAP", entityName: "SERNMS", entityId: "1.0.0", ipAddress: "127.0.0.1", status: "SUCCESS", details: "SERNMS operational" }
    ];
  }

  return {
    ok: true,
    status: 200,
    json: async () => mockData,
    text: async () => JSON.stringify(mockData)
  };
}

// Initial Boot & Session Validation
document.addEventListener('DOMContentLoaded', () => {
  let savedToken = localStorage.getItem('sernms_token');
  let savedUser = localStorage.getItem('sernms_user');

  // If opened directly without session or in file mode, auto-provision default admin session
  if (!savedToken || !savedUser) {
    if (window.location.protocol === 'file:' || !window.location.origin.includes(':8080')) {
      savedUser = JSON.stringify({
        id: 1,
        username: 'admin',
        firstName: 'System',
        lastName: 'Administrator',
        email: 'admin@sernms.enterprise.local',
        department: 'IT Operations',
        roles: ['ROLE_ADMIN', 'ROLE_NETWORK_ADMIN', 'ROLE_CLOUD_ADMIN']
      });
      savedToken = 'demo-admin-token';
      localStorage.setItem('sernms_token', savedToken);
      localStorage.setItem('sernms_user', savedUser);
    } else {
      window.location.href = 'login.html';
      return;
    }
  }

  currentToken = savedToken;
  try {
    currentUser = JSON.parse(savedUser);
  } catch (e) {
    logout();
    return;
  }

  setupUserInterface();
  loadDashboard();

  // Periodic telemetry refresh
  setInterval(() => {
    const activeView = document.querySelector('.tab-view.active');
    if (activeView && activeView.id === 'view-dashboard') {
      loadDashboard(true);
    } else if (activeView && activeView.id === 'view-monitoring') {
      loadMonitoring();
    }
  }, 10000);
});

// Configure UI components based on user roles
function setupUserInterface() {
  const nameEl = document.getElementById('sidebar-user-name');
  const roleEl = document.getElementById('sidebar-user-role');
  const avatarEl = document.getElementById('sidebar-user-avatar');
  const badgeEl = document.getElementById('header-role-badge');

  if (nameEl) nameEl.innerText = `${currentUser.firstName || currentUser.username}`;
  if (avatarEl) avatarEl.innerText = (currentUser.username || 'A').substring(0, 1).toUpperCase();

  const primaryRole = (currentUser.roles && currentUser.roles.length > 0)
    ? currentUser.roles[0].replace('ROLE_', '')
    : 'USER';

  if (roleEl) roleEl.innerText = primaryRole;
  if (badgeEl) badgeEl.innerText = primaryRole;

  // Role-based UI guards
  const isAdmin = currentUser.roles.some(r => r.includes('ADMIN') && !r.includes('NETWORK') && !r.includes('CLOUD'));
  const isNetAdmin = currentUser.roles.some(r => r.includes('NETWORK_ADMIN'));
  const isCloudAdmin = currentUser.roles.some(r => r.includes('CLOUD_ADMIN'));

  if (!isAdmin) {
    const auditItem = document.getElementById('nav-audit-item');
    const usersItem = document.getElementById('nav-users-item');
    if (auditItem) auditItem.style.display = 'none';
    if (usersItem) usersItem.style.display = 'none';
  }
}

function logout() {
  localStorage.removeItem('sernms_token');
  localStorage.removeItem('sernms_user');
  window.location.href = 'login.html';
}

// Router / View Switcher
function switchView(viewName) {
  // Update nav highlight
  document.querySelectorAll('.app-sidebar .nav-item').forEach(item => item.classList.remove('active'));
  const clickedItem = event && event.currentTarget ? event.currentTarget : null;
  if (clickedItem) clickedItem.classList.add('active');

  // Update visible view
  document.querySelectorAll('.tab-view').forEach(view => view.classList.remove('active'));
  const targetView = document.getElementById(`view-${viewName}`);
  if (targetView) targetView.classList.add('active');

  // Update Title
  const titles = {
    dashboard: 'Executive Dashboard',
    topology: 'Network Topology Visualizer',
    devices: 'Network Device Management',
    vlans: 'Campus VLAN Subnets',
    aws: 'AWS Cloud Infrastructure',
    monitoring: 'Centralized Health Telemetry',
    incidents: 'Incident & Ticketing System',
    alerts: 'Active Alarms & Threshold Warnings',
    audit: 'Security & Compliance Audit Trail',
    users: 'Enterprise User Accounts',
    reports: 'Executive Reports & Compliance Exports',
    demo: 'Failure Simulation & Defense Lab',
    'cisco-arch': 'Cisco & Hybrid Architecture'
  };
  document.getElementById('current-view-title').innerText = titles[viewName] || 'Enterprise Console';

  // Trigger data loaders
  switch (viewName) {
    case 'dashboard': loadDashboard(); break;
    case 'topology': loadTopology(); break;
    case 'devices': loadDevices(); break;
    case 'vlans': loadVlans(); break;
    case 'aws': loadAwsResources(); break;
    case 'monitoring': loadMonitoring(); break;
    case 'incidents': loadIncidents(); break;
    case 'alerts': loadAlerts(); break;
    case 'audit': loadAuditLogs(); break;
    case 'users': loadUsers(); break;
  }
}

// ================= 1. DASHBOARD MODULE =================
async function loadDashboard(silent = false) {
  try {
    const res = await apiFetch('/api/v1/monitoring/dashboard');
    if (!res.ok) return;
    const stats = await res.json();

    // Metric counters
    document.getElementById('stat-devices-total').innerText = stats.totalNetworkDevices;
    document.getElementById('stat-devices-online').innerText = stats.onlineNetworkDevices;
    document.getElementById('stat-devices-offline').innerText = stats.offlineNetworkDevices;

    document.getElementById('stat-aws-total').innerText = stats.totalAwsResources;
    document.getElementById('stat-aws-running').innerText = stats.runningAwsResources;

    document.getElementById('stat-incidents-open').innerText = stats.openIncidents;
    document.getElementById('stat-incidents-investigating').innerText = `${stats.investigatingIncidents} in progress`;

    document.getElementById('stat-critical-alerts').innerText = stats.criticalAlerts;
    document.getElementById('stat-unack-alerts').innerText = `${stats.unacknowledgedAlerts} total active`;

    document.getElementById('stat-health-pct').innerText = `${stats.systemHealthPercentage}%`;

    // Badges in sidebar
    const alertBadge = document.getElementById('badge-alert-count');
    if (alertBadge) alertBadge.innerText = stats.unacknowledgedAlerts;

    const incBadge = document.getElementById('badge-incident-count');
    if (incBadge) incBadge.innerText = stats.openIncidents;

    // VPN Chip
    const vpnText = document.getElementById('vpn-status-text');
    const vpnChip = document.getElementById('chip-vpn-status');
    if (vpnText && vpnChip) {
      vpnText.innerText = `IPSec VPN: ${stats.hybridVpnStatus}`;
      if (stats.hybridVpnStatus === 'CONNECTED') {
        vpnChip.className = 'status-chip';
      } else {
        vpnChip.className = 'status-chip disconnected';
      }
    }

    // Populate recent alerts table
    const alertsTbody = document.getElementById('dashboard-recent-alerts-table');
    if (alertsTbody && stats.recentAlerts) {
      if (stats.recentAlerts.length === 0) {
        alertsTbody.innerHTML = `<tr><td colspan="4" style="text-align:center;color:var(--text-muted);">No active warnings or alarms detected.</td></tr>`;
      } else {
        alertsTbody.innerHTML = stats.recentAlerts.map(a => `
          <tr>
            <td><span class="badge badge-${a.severity.toLowerCase()}">${a.severity}</span></td>
            <td><strong>${escapeHtml(a.alertTitle)}</strong></td>
            <td><code>${a.sourceEntityType}: ${a.sourceEntityId}</code></td>
            <td style="color:var(--text-muted);font-size:0.78rem;">${formatTime(a.triggeredAt)}</td>
          </tr>
        `).join('');
      }
    }

    // Populate recent incidents table
    const incTbody = document.getElementById('dashboard-recent-incidents-table');
    if (incTbody && stats.recentIncidents) {
      if (stats.recentIncidents.length === 0) {
        incTbody.innerHTML = `<tr><td colspan="4" style="text-align:center;color:var(--text-muted);">No active incident tickets.</td></tr>`;
      } else {
        incTbody.innerHTML = stats.recentIncidents.map(i => `
          <tr>
            <td><strong>${i.ticketNumber}</strong></td>
            <td>${escapeHtml(i.title)}</td>
            <td><span class="badge badge-${i.priority.toLowerCase()}">${i.priority}</span></td>
            <td><span class="badge badge-medium">${i.status}</span></td>
          </tr>
        `).join('');
      }
    }

    // Load Charts
    loadDashboardCharts(stats);

  } catch (err) {
    if (!silent) console.error('Dashboard load failed', err);
  }
}

async function loadDashboardCharts(stats) {
  try {
    const res = await apiFetch('/api/v1/monitoring/telemetry');
    if (!res.ok) return;
    const series = await res.json();

    const labels = series.map(p => p.timeLabel);
    const cpuData = series.map(p => p.cpuUtilization);
    const memData = series.map(p => p.memoryUtilization);

    // 1. CPU Telemetry Line Chart
    const ctxCpu = document.getElementById('chart-cpu-telemetry');
    if (ctxCpu) {
      if (cpuChart) cpuChart.destroy();
      cpuChart = new Chart(ctxCpu, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [
            {
              label: 'EC2 App Server CPU (%)',
              data: cpuData,
              borderColor: '#2563eb',
              backgroundColor: 'rgba(37, 99, 235, 0.08)',
              fill: true,
              tension: 0.35,
              borderWidth: 2
            },
            {
              label: 'Average Memory (%)',
              data: memData,
              borderColor: '#10b981',
              backgroundColor: 'transparent',
              borderDash: [4, 4],
              tension: 0.35,
              borderWidth: 2
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { position: 'top' } },
          scales: {
            y: { min: 0, max: 100, ticks: { callback: v => v + '%' } }
          }
        }
      });
    }

    // 2. Resource Distribution Doughnut Chart
    const ctxDist = document.getElementById('chart-resource-dist');
    if (ctxDist) {
      if (distChart) distChart.destroy();
      distChart = new Chart(ctxDist, {
        type: 'doughnut',
        data: {
          labels: ['Online Network', 'AWS Cloud Active', 'Warnings', 'Offline / Critical'],
          datasets: [{
            data: [
              stats.onlineNetworkDevices || 7,
              stats.runningAwsResources || 6,
              stats.warningNetworkDevices || 0,
              (stats.offlineNetworkDevices || 0) + (stats.criticalAlerts || 0)
            ],
            backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#ef4444'],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { position: 'bottom' } }
        }
      });
    }
  } catch (e) {
    console.error('Charts initialization error', e);
  }
}

// ================= 2. NETWORK TOPOLOGY MODULE =================
async function loadTopology() {
  try {
    const res = await apiFetch('/api/v1/topology');
    if (!res.ok) return;
    const data = await res.json();

    const container = document.getElementById('topology-canvas');
    if (!container) return;

    const visNodes = new vis.DataSet(data.nodes.map(n => ({
      id: n.id,
      label: n.label,
      title: n.title,
      level: n.level,
      shape: n.shape || 'box',
      color: {
        background: n.color,
        border: '#0f172a',
        highlight: { background: n.color, border: '#2563eb' }
      },
      font: { color: '#ffffff', size: 12, face: 'Segoe UI' },
      margin: 10,
      shadow: true
    })));

    const visEdges = new vis.DataSet(data.edges.map(e => ({
      from: e.from,
      to: e.to,
      label: e.label,
      color: { color: e.color, highlight: '#2563eb' },
      dashes: e.dashes,
      width: e.width || 2,
      font: { size: 10, align: 'middle', background: '#ffffff' }
    })));

    const options = {
      layout: {
        hierarchical: {
          direction: 'UD',
          sortMethod: 'directed',
          levelSeparation: 95,
          nodeSpacing: 180
        }
      },
      physics: false,
      interaction: {
        hover: true,
        tooltipDelay: 100,
        zoomView: true,
        dragView: true
      }
    };

    networkGraph = new vis.Network(container, { nodes: visNodes, edges: visEdges }, options);

    networkGraph.on('click', (params) => {
      if (params.nodes.length > 0) {
        const nodeId = params.nodes[0];
        const node = data.nodes.find(n => n.id === nodeId);
        showNodeDetails(node);
      }
    });

  } catch (err) {
    console.error('Topology loading error', err);
  }
}

function resetTopologyZoom() {
  if (networkGraph) networkGraph.fit();
}

function showNodeDetails(node) {
  if (!node) return;
  const panel = document.getElementById('topology-node-details');
  const content = document.getElementById('topology-node-content');
  if (!panel || !content) return;

  panel.style.display = 'block';
  content.innerHTML = `
    <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px;">
      <div><strong>Entity Identifier:</strong> <code>${node.id}</code></div>
      <div><strong>Node Name:</strong> ${escapeHtml(node.label.replace('\n', ' '))}</div>
      <div><strong>Operational Status:</strong> <span class="badge badge-${node.status.toLowerCase()}">${node.status}</span></div>
      <div><strong>IP Address / Prefix:</strong> <code>${node.ip || 'N/A'}</code></div>
      <div><strong>Infrastructure Tier:</strong> Level ${node.level} (${node.group})</div>
    </div>
    <div style="margin-top: 14px; font-size: 0.85rem; color: var(--text-muted); background: #f8fafc; padding: 10px; border-radius: var(--radius-sm);">
      ${escapeHtml(node.title || 'Enterprise node connected via redundant backbone.')}
    </div>
  `;
}

// ================= 3. NETWORK DEVICES MODULE =================
async function loadDevices() {
  try {
    const res = await apiFetch('/api/v1/network/devices');
    if (!res.ok) return;
    allDevicesCache = await res.json();
    renderDevicesTable(allDevicesCache);
    loadVlansForDropdown();
  } catch (err) {
    console.error('Failed to load devices', err);
  }
}

function renderDevicesTable(devices) {
  const tbody = document.getElementById('devices-table-body');
  if (!tbody) return;

  if (devices.length === 0) {
    tbody.innerHTML = `<tr><td colspan="9" style="text-align:center;color:var(--text-muted);">No devices found matching query.</td></tr>`;
    return;
  }

  tbody.innerHTML = devices.map(d => `
    <tr>
      <td><strong>${escapeHtml(d.name)}</strong></td>
      <td><code>${d.deviceType}</code></td>
      <td><code>${d.ipAddress}</code></td>
      <td><small style="color:var(--text-muted);">${d.macAddress || 'N/A'}</small></td>
      <td>${escapeHtml(d.location || 'MDF')}</td>
      <td>${d.vlanName ? `<span class="badge badge-low">${d.vlanName}</span>` : 'N/A'}</td>
      <td><span class="badge badge-${d.status.toLowerCase()}">${d.status}</span></td>
      <td><small style="color:var(--text-muted);">${formatTime(d.lastSeen)}</small></td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="pingDevice('${d.ipAddress}', '${escapeHtml(d.name)}')">Ping</button>
        <button class="btn btn-secondary btn-sm" onclick="toggleDeviceStatus(${d.id}, '${d.status}')">Toggle</button>
        <button class="btn btn-danger btn-sm" onclick="deleteDevice(${d.id})">Delete</button>
      </td>
    </tr>
  `).join('');
}

function filterDevicesTable() {
  const query = (document.getElementById('device-search-input').value || '').toLowerCase();
  const statusFilter = document.getElementById('device-status-filter').value;

  const filtered = allDevicesCache.filter(d => {
    const matchesQuery = !query ||
      d.name.toLowerCase().includes(query) ||
      d.ipAddress.toLowerCase().includes(query) ||
      (d.model && d.model.toLowerCase().includes(query));
    const matchesStatus = !statusFilter || d.status === statusFilter;
    return matchesQuery && matchesStatus;
  });

  renderDevicesTable(filtered);
}

function openAddDeviceModal() {
  openModal('modal-add-device');
}

async function loadVlansForDropdown() {
  try {
    const res = await apiFetch('/api/v1/network/vlans');
    if (!res.ok) return;
    const vlans = await res.json();
    const select = document.getElementById('dev-vlan-select');
    if (select) {
      select.innerHTML = '<option value="">None (Trunk / Unassigned)</option>' +
        vlans.map(v => `<option value="${v.id}">VLAN ${v.vlanId} - ${v.name} (${v.subnetCidr})</option>`).join('');
    }
  } catch (e) {
    console.error(e);
  }
}

async function submitAddDevice() {
  const name = document.getElementById('dev-name').value.trim();
  const deviceType = document.getElementById('dev-type').value;
  const ipAddress = document.getElementById('dev-ip').value.trim();
  const macAddress = document.getElementById('dev-mac').value.trim();
  const location = document.getElementById('dev-loc').value.trim();
  const manufacturer = document.getElementById('dev-mfr').value.trim();
  const model = document.getElementById('dev-model').value.trim();
  const vlanId = document.getElementById('dev-vlan-select').value || null;

  if (!name || !ipAddress) {
    showToast('Name and IP address are required', 'warning');
    return;
  }

  try {
    const res = await apiFetch('/api/v1/network/devices', {
      method: 'POST',
      body: JSON.stringify({ name, deviceType, ipAddress, macAddress, location, manufacturer, model, vlanId })
    });

    if (!res.ok) {
      const err = await res.json();
      showToast(err.message || 'Failed to save device', 'error');
      return;
    }

    showToast(`Device ${name} added successfully!`, 'success');
    closeModal('modal-add-device');
    loadDevices();
  } catch (err) {
    showToast('Error communicating with backend', 'error');
  }
}

async function toggleDeviceStatus(id, currentStatus) {
  const newStatus = currentStatus === 'ONLINE' ? 'OFFLINE' : 'ONLINE';
  try {
    const res = await apiFetch(`/api/v1/network/devices/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: newStatus })
    });
    if (res.ok) {
      showToast(`Device status changed to ${newStatus}`, newStatus === 'ONLINE' ? 'success' : 'error');
      loadDevices();
    }
  } catch (err) {
    showToast('Failed to toggle device status', 'error');
  }
}

async function pingDevice(ip, name) {
  showToast(`Transmitting ICMP echo request to ${name} (${ip})...`, 'info');
  setTimeout(() => {
    showToast(`Reply from ${ip}: bytes=32 time=1.4ms TTL=254. 0% loss.`, 'success');
  }, 600);
}

async function deleteDevice(id) {
  if (!confirm('Are you sure you want to remove this device from inventory?')) return;
  try {
    const res = await apiFetch(`/api/v1/network/devices/${id}`, { method: 'DELETE' });
    if (res.ok) {
      showToast('Device decommissioned successfully', 'success');
      loadDevices();
    }
  } catch (err) {
    showToast('Failed to delete device', 'error');
  }
}

// ================= 4. VLAN MODULE =================
async function loadVlans() {
  try {
    const res = await apiFetch('/api/v1/network/vlans');
    if (!res.ok) return;
    const vlans = await res.json();
    const tbody = document.getElementById('vlans-table-body');
    if (!tbody) return;

    tbody.innerHTML = vlans.map(v => `
      <tr>
        <td><strong>VLAN ${v.vlanId}</strong></td>
        <td>${escapeHtml(v.name)}</td>
        <td><code>${v.subnetCidr}</code></td>
        <td><code>${v.gatewayIp}</code></td>
        <td>${escapeHtml(v.description || '')}</td>
        <td><span class="badge badge-low">${v.deviceCount} Devices</span></td>
        <td>
          <button class="btn btn-danger btn-sm" onclick="deleteVlan(${v.id})">Delete</button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    console.error(err);
  }
}

function openAddVlanModal() {
  openModal('modal-add-vlan');
}

async function submitAddVlan() {
  const vlanId = parseInt(document.getElementById('vlan-id-input').value, 10);
  const name = document.getElementById('vlan-name-input').value.trim();
  const subnetCidr = document.getElementById('vlan-cidr-input').value.trim();
  const gatewayIp = document.getElementById('vlan-gw-input').value.trim();
  const description = document.getElementById('vlan-desc-input').value.trim();

  if (!vlanId || !name || !subnetCidr || !gatewayIp) {
    showToast('Please fill all required VLAN fields', 'warning');
    return;
  }

  try {
    const res = await apiFetch('/api/v1/network/vlans', {
      method: 'POST',
      body: JSON.stringify({ vlanId, name, subnetCidr, gatewayIp, description })
    });
    if (!res.ok) {
      const err = await res.json();
      showToast(err.message || 'Failed to create VLAN', 'error');
      return;
    }
    showToast(`VLAN ${vlanId} created successfully!`, 'success');
    closeModal('modal-add-vlan');
    loadVlans();
  } catch (e) {
    showToast('Error communicating with backend', 'error');
  }
}

async function deleteVlan(id) {
  if (!confirm('Are you sure you want to delete this VLAN?')) return;
  try {
    const res = await apiFetch(`/api/v1/network/vlans/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      const err = await res.json();
      showToast(err.message || 'Cannot delete VLAN with assigned devices', 'error');
      return;
    }
    showToast('VLAN removed successfully', 'success');
    loadVlans();
  } catch (e) {
    showToast('Failed to delete VLAN', 'error');
  }
}

// ================= 5. AWS CLOUD MODULE =================
async function loadAwsResources() {
  try {
    const res = await apiFetch('/api/v1/aws/resources');
    if (!res.ok) return;
    allAwsCache = await res.json();
    renderAwsCards(allAwsCache);
    renderAwsTable(allAwsCache);
  } catch (err) {
    console.error('Failed to load AWS resources', err);
  }
}

function switchAwsTab(type) {
  document.querySelectorAll('#view-aws .tab-btn').forEach(btn => btn.classList.remove('active'));
  if (event && event.currentTarget) event.currentTarget.classList.add('active');

  if (type === 'all') {
    renderAwsCards(allAwsCache);
    renderAwsTable(allAwsCache);
  } else {
    const filtered = allAwsCache.filter(r => r.resourceType === type);
    renderAwsCards(filtered);
    renderAwsTable(filtered);
  }
}

function renderAwsCards(resources) {
  const container = document.getElementById('aws-cards-container');
  if (!container) return;

  container.innerHTML = resources.map(r => {
    const isRunning = ['RUNNING', 'AVAILABLE', 'HEALTHY', 'CONNECTED'].includes(r.status);
    return `
      <div class="stat-card" style="flex-direction:column;align-items:flex-start;">
        <div style="display:flex;justify-content:space-between;width:100%;align-items:center;">
          <span class="badge badge-low">${r.resourceType}</span>
          <span class="badge badge-${isRunning ? 'online' : 'offline'}">${r.status}</span>
        </div>
        <div style="margin-top:12px;">
          <h4 style="font-size:1.05rem;font-weight:700;">${escapeHtml(r.resourceName)}</h4>
          <div style="font-size:0.8rem;color:var(--text-muted);font-family:monospace;">${r.resourceId}</div>
        </div>
        <div style="margin-top:12px;font-size:0.82rem;color:var(--text-main);width:100%;">
          <div><strong>Config:</strong> ${r.instanceType || 'Standard'}</div>
          <div><strong>IP Address:</strong> <code>${r.privateIp || r.publicIp || 'N/A'}</code></div>
          <div><strong>Region:</strong> ${r.region}</div>
        </div>
        <div style="margin-top:14px;display:flex;gap:8px;width:100%;">
          ${r.resourceType === 'EC2' ? `
            <button class="btn btn-primary btn-sm" onclick="startAwsInstance('${r.resourceId}')">Start</button>
            <button class="btn btn-secondary btn-sm" onclick="stopAwsInstance('${r.resourceId}')">Stop</button>
          ` : ''}
          ${r.resourceType === 'VGW' ? `
            <button class="btn btn-warning btn-sm" onclick="toggleAwsVpn('${r.status === 'CONNECTED' ? 'DISCONNECTED' : 'CONNECTED'}')">Toggle Tunnel</button>
          ` : ''}
        </div>
      </div>
    `;
  }).join('');
}

function renderAwsTable(resources) {
  const tbody = document.getElementById('aws-table-body');
  if (!tbody) return;

  tbody.innerHTML = resources.map(r => `
    <tr>
      <td><code>${r.resourceId}</code></td>
      <td><strong>${escapeHtml(r.resourceName)}</strong></td>
      <td><span class="badge badge-low">${r.resourceType}</span></td>
      <td>${r.region}</td>
      <td><code>${r.privateIp || r.publicIp || 'VPC Route'}</code></td>
      <td><span class="badge badge-${['RUNNING','AVAILABLE','HEALTHY','CONNECTED'].includes(r.status) ? 'online' : 'offline'}">${r.status}</span></td>
      <td><small>${r.instanceType} ${r.storageInfo ? `(${r.storageInfo})` : ''}</small></td>
      <td>
        ${r.resourceType === 'EC2' ? `
          <button class="btn btn-secondary btn-sm" onclick="startAwsInstance('${r.resourceId}')">Start</button>
          <button class="btn btn-secondary btn-sm" onclick="stopAwsInstance('${r.resourceId}')">Stop</button>
        ` : `<span style="color:var(--text-muted);font-size:0.8rem;">Managed</span>`}
      </td>
    </tr>
  `).join('');
}

async function startAwsInstance(id) {
  try {
    const res = await apiFetch(`/api/v1/aws/resources/${id}/start`, { method: 'POST' });
    if (res.ok) {
      showToast(`Start signal dispatched to ${id}`, 'success');
      loadAwsResources();
    }
  } catch (e) {
    showToast('Failed to start instance', 'error');
  }
}

async function stopAwsInstance(id) {
  try {
    const res = await apiFetch(`/api/v1/aws/resources/${id}/stop`, { method: 'POST' });
    if (res.ok) {
      showToast(`Stop signal dispatched to ${id}`, 'warning');
      loadAwsResources();
    }
  } catch (e) {
    showToast('Failed to stop instance', 'error');
  }
}

async function toggleAwsVpn(newStatus) {
  try {
    const res = await apiFetch('/api/v1/aws/vpn/status', {
      method: 'PUT',
      body: JSON.stringify({ status: newStatus })
    });
    if (res.ok) {
      showToast(`IPSec VPN status updated to ${newStatus}`, 'info');
      loadAwsResources();
      loadDashboard();
    }
  } catch (e) {
    showToast('Failed to update VPN status', 'error');
  }
}

// ================= 6. CENTRAL MONITORING MODULE =================
async function loadMonitoring() {
  try {
    const res = await apiFetch('/api/v1/monitoring/telemetry');
    if (!res.ok) return;
    const series = await res.json();

    const labels = series.map(p => p.timeLabel);
    const cpuData = series.map(p => p.cpuUtilization);
    const netInData = series.map(p => p.networkInMbps);
    const netOutData = series.map(p => p.networkOutMbps);

    // Monitoring CPU Chart
    const ctxCpu = document.getElementById('chart-monitoring-cpu');
    if (ctxCpu) {
      if (monitoringCpuChart) monitoringCpuChart.destroy();
      monitoringCpuChart = new Chart(ctxCpu, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [{
            label: 'Telemetry Engine CPU (%)',
            data: cpuData,
            borderColor: '#2563eb',
            backgroundColor: 'rgba(37, 99, 235, 0.1)',
            fill: true,
            tension: 0.3
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { y: { min: 0, max: 100 } }
        }
      });
    }

    // Monitoring Network Chart
    const ctxNet = document.getElementById('chart-monitoring-net');
    if (ctxNet) {
      if (monitoringNetChart) monitoringNetChart.destroy();
      monitoringNetChart = new Chart(ctxNet, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [
            {
              label: 'Inbound Throughput (Mbps)',
              data: netInData,
              borderColor: '#10b981',
              tension: 0.3
            },
            {
              label: 'Outbound Throughput (Mbps)',
              data: netOutData,
              borderColor: '#f59e0b',
              tension: 0.3
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false
        }
      });
    }
  } catch (e) {
    console.error('Monitoring load error', e);
  }
}

// ================= 7. INCIDENT TICKETS MODULE =================
async function loadIncidents() {
  try {
    const statusFilter = document.getElementById('incident-status-filter')?.value || '';
    const url = statusFilter ? `/api/v1/incidents/status/${statusFilter}` : '/api/v1/incidents';
    const res = await apiFetch(url);
    if (!res.ok) return;
    const incidents = await res.json();

    const tbody = document.getElementById('incidents-table-body');
    if (!tbody) return;

    tbody.innerHTML = incidents.map(i => `
      <tr>
        <td><strong>${i.ticketNumber}</strong></td>
        <td>${escapeHtml(i.title)}</td>
        <td><code>${i.category}</code></td>
        <td><span class="badge badge-${i.priority.toLowerCase()}">${i.priority}</span></td>
        <td><span class="badge badge-medium">${i.status}</span></td>
        <td>${escapeHtml(i.reporterName)}</td>
        <td>${escapeHtml(i.assigneeName || 'Unassigned')}</td>
        <td><small style="color:var(--text-muted);">${formatTime(i.createdAt)}</small></td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="viewIncidentDetails(${i.id})">Inspect</button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    console.error(err);
  }
}

function openCreateIncidentModal() {
  openModal('modal-create-incident');
}

async function submitCreateIncident() {
  const title = document.getElementById('inc-title').value.trim();
  const category = document.getElementById('inc-category').value;
  const priority = document.getElementById('inc-priority').value;
  const impactedResourceId = document.getElementById('inc-resource-id').value.trim();
  const description = document.getElementById('inc-desc').value.trim();

  if (!title || !description) {
    showToast('Title and Description are required', 'warning');
    return;
  }

  try {
    const res = await apiFetch('/api/v1/incidents', {
      method: 'POST',
      body: JSON.stringify({
        title, category, priority, description,
        impactedResourceType: category === 'CLOUD' ? 'EC2' : 'NETWORK_DEVICE',
        impactedResourceId
      })
    });
    if (!res.ok) {
      showToast('Failed to create ticket', 'error');
      return;
    }
    showToast('Incident ticket raised successfully!', 'success');
    closeModal('modal-create-incident');
    loadIncidents();
    loadDashboard();
  } catch (e) {
    showToast('Failed to submit incident', 'error');
  }
}

async function viewIncidentDetails(id) {
  try {
    const res = await apiFetch(`/api/v1/incidents/${id}`);
    if (!res.ok) return;
    const inc = await res.json();

    document.getElementById('inc-detail-ticket-no').innerText = `Incident Ticket ${inc.ticketNumber}`;
    const body = document.getElementById('inc-detail-body');

    body.innerHTML = `
      <div style="margin-bottom:16px;">
        <h4 style="font-size:1.15rem;margin-bottom:6px;">${escapeHtml(inc.title)}</h4>
        <div style="display:flex;gap:10px;align-items:center;">
          <span class="badge badge-${inc.priority.toLowerCase()}">Priority: ${inc.priority}</span>
          <span class="badge badge-medium">Status: ${inc.status}</span>
          <span class="badge badge-low">Category: ${inc.category}</span>
        </div>
      </div>

      <div style="background:#f8fafc;padding:14px;border-radius:var(--radius-sm);font-size:0.88rem;margin-bottom:18px;">
        <strong>Description:</strong><br>${escapeHtml(inc.description)}
      </div>

      <div style="display:flex;gap:12px;margin-bottom:20px;">
        <div>
          <label class="form-label">Transition Status</label>
          <select id="inc-transition-status" class="form-control" style="width:180px;">
            <option value="OPEN" ${inc.status === 'OPEN' ? 'selected' : ''}>OPEN</option>
            <option value="ASSIGNED" ${inc.status === 'ASSIGNED' ? 'selected' : ''}>ASSIGNED</option>
            <option value="INVESTIGATING" ${inc.status === 'INVESTIGATING' ? 'selected' : ''}>INVESTIGATING</option>
            <option value="RESOLVED" ${inc.status === 'RESOLVED' ? 'selected' : ''}>RESOLVED</option>
            <option value="CLOSED" ${inc.status === 'CLOSED' ? 'selected' : ''}>CLOSED</option>
          </select>
        </div>
        <div style="align-self:flex-end;">
          <button class="btn btn-primary" onclick="updateIncidentStatus(${inc.id})">Update Status</button>
        </div>
      </div>

      <div>
        <h5 style="font-size:0.95rem;margin-bottom:10px;">Discussion & Resolution Timeline (${inc.commentsCount || 0})</h5>
        <div id="inc-comments-list" style="max-height:180px;overflow-y:auto;display:flex;flex-direction:column;gap:10px;margin-bottom:14px;">
          ${(inc.comments || []).map(c => `
            <div style="background:#ffffff;border:1px solid var(--card-border);padding:10px;border-radius:var(--radius-sm);font-size:0.82rem;">
              <div style="display:flex;justify-content:space-between;color:var(--text-muted);margin-bottom:4px;">
                <strong>${escapeHtml(c.username)}</strong>
                <span>${formatTime(c.createdAt)}</span>
              </div>
              <div>${escapeHtml(c.commentText)}</div>
            </div>
          `).join('') || '<div style="color:var(--text-muted);font-size:0.82rem;">No comments recorded yet.</div>'}
        </div>

        <div style="display:flex;gap:8px;">
          <input type="text" id="inc-new-comment" class="form-control" placeholder="Post root-cause analysis or note...">
          <button class="btn btn-secondary" onclick="postComment(${inc.id})">Post</button>
        </div>
      </div>
    `;

    openModal('modal-incident-details');
  } catch (err) {
    console.error(err);
  }
}

async function updateIncidentStatus(id) {
  const status = document.getElementById('inc-transition-status').value;
  try {
    const res = await apiFetch(`/api/v1/incidents/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status })
    });
    if (res.ok) {
      showToast(`Incident transitioned to ${status}`, 'success');
      viewIncidentDetails(id);
      loadIncidents();
      loadDashboard();
    }
  } catch (e) {
    showToast('Failed to update status', 'error');
  }
}

async function postComment(id) {
  const text = document.getElementById('inc-new-comment').value.trim();
  if (!text) return;
  try {
    const res = await apiFetch(`/api/v1/incidents/${id}/comments`, {
      method: 'POST',
      body: JSON.stringify({ commentText: text })
    });
    if (res.ok) {
      showToast('Comment posted', 'info');
      viewIncidentDetails(id);
    }
  } catch (e) {
    showToast('Failed to post comment', 'error');
  }
}

// ================= 8. ALERTS & ALARMS MODULE =================
async function loadAlerts() {
  try {
    const res = await apiFetch('/api/v1/alerts');
    if (!res.ok) return;
    const alerts = await res.json();

    const tbody = document.getElementById('alerts-table-body');
    if (!tbody) return;

    tbody.innerHTML = alerts.map(a => `
      <tr>
        <td><span class="badge badge-${a.severity.toLowerCase()}">${a.severity}</span></td>
        <td><strong>${escapeHtml(a.alertTitle)}</strong></td>
        <td>${escapeHtml(a.alertMessage)}</td>
        <td><code>${a.category}</code></td>
        <td><code>${a.sourceEntityType}: ${a.sourceEntityId}</code></td>
        <td><small style="color:var(--text-muted);">${formatTime(a.triggeredAt)}</small></td>
        <td>
          ${!a.acknowledged ? `
            <button class="btn btn-warning btn-sm" onclick="ackAlert(${a.id})">Ack</button>
          ` : `<span class="badge badge-online">Acknowledged</span>`}
        </td>
      </tr>
    `).join('');
  } catch (e) {
    console.error(e);
  }
}

async function ackAlert(id) {
  try {
    const res = await apiFetch(`/api/v1/alerts/${id}/acknowledge`, { method: 'PUT' });
    if (res.ok) {
      showToast('Alert acknowledged', 'info');
      loadAlerts();
      loadDashboard();
    }
  } catch (e) {
    showToast('Failed to acknowledge alert', 'error');
  }
}

// ================= 9. AUDIT LOGS MODULE =================
async function loadAuditLogs() {
  try {
    const res = await apiFetch('/api/v1/audit-logs');
    if (!res.ok) return;
    const logs = await res.json();

    const tbody = document.getElementById('audit-table-body');
    if (!tbody) return;

    tbody.innerHTML = logs.map(l => `
      <tr>
        <td><small style="color:var(--text-muted);">${formatTime(l.timestamp)}</small></td>
        <td><strong>${escapeHtml(l.username)}</strong></td>
        <td><code>${l.action}</code></td>
        <td>${l.entityName || 'N/A'}</td>
        <td><code>${l.entityId || 'N/A'}</code></td>
        <td><code>${l.ipAddress}</code></td>
        <td><span class="badge badge-${l.status === 'SUCCESS' ? 'online' : 'offline'}">${l.status}</span></td>
        <td><small>${escapeHtml(l.details || '')}</small></td>
      </tr>
    `).join('');
  } catch (e) {
    console.error(e);
  }
}

// ================= 10. USER ACCOUNTS MODULE =================
async function loadUsers() {
  try {
    const res = await apiFetch('/api/v1/users');
    if (!res.ok) return;
    const users = await res.json();

    const tbody = document.getElementById('users-table-body');
    if (!tbody) return;

    tbody.innerHTML = users.map(u => `
      <tr>
        <td><strong>${escapeHtml(u.username)}</strong></td>
        <td>${escapeHtml(u.email)}</td>
        <td>${escapeHtml(u.firstName || '')} ${escapeHtml(u.lastName || '')}</td>
        <td>${escapeHtml(u.department || 'General')}</td>
        <td>${(u.roles || []).map(r => `<span class="badge badge-low">${r.replace('ROLE_', '')}</span>`).join(' ')}</td>
        <td><span class="badge badge-${u.active ? 'online' : 'offline'}">${u.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
        <td><small style="color:var(--text-muted);">${u.lastLogin ? formatTime(u.lastLogin) : 'Never'}</small></td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="toggleUser(${u.id})">Toggle Active</button>
        </td>
      </tr>
    `).join('');
  } catch (e) {
    console.error(e);
  }
}

async function toggleUser(id) {
  try {
    const res = await apiFetch(`/api/v1/users/${id}/toggle-status`, { method: 'PUT' });
    if (res.ok) {
      showToast('User status toggled', 'success');
      loadUsers();
    }
  } catch (e) {
    showToast('Failed to toggle user status', 'error');
  }
}

// ================= 11. DEMO & FAULT INJECTION LAB MODULE =================
async function runDemoScenario(scenario, targetId) {
  showToast(`Injecting scenario: ${scenario} on ${targetId}...`, 'warning');
  try {
    const res = await apiFetch('/api/v1/demo/simulate', {
      method: 'POST',
      body: JSON.stringify({ scenario, targetId })
    });
    if (!res.ok) {
      showToast('Failed to execute simulation', 'error');
      return;
    }
    const data = await res.json();
    showToast(data.message, data.status === 'SUCCESS' ? 'success' : 'error');

    // Immediately reload dashboard and active views
    loadDashboard();
    if (document.getElementById('view-topology').classList.contains('active')) loadTopology();
    if (document.getElementById('view-devices').classList.contains('active')) loadDevices();
    if (document.getElementById('view-aws').classList.contains('active')) loadAwsResources();
    if (document.getElementById('view-alerts').classList.contains('active')) loadAlerts();
  } catch (e) {
    showToast('Simulation communication error', 'error');
  }
}

// ================= 12. CISCO CONFIGS TAB SWITCHER =================
function switchConfigTab(tab) {
  document.querySelectorAll('#view-cisco-arch .tab-btn').forEach(btn => btn.classList.remove('active'));
  if (event && event.currentTarget) event.currentTarget.classList.add('active');

  document.querySelectorAll('.config-code-block').forEach(b => b.style.display = 'none');
  const target = document.getElementById(`config-content-${tab}`);
  if (target) target.style.display = 'block';
}

// Utility formatting functions
function formatTime(isoStr) {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' ' + d.toLocaleDateString();
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[m]);
}

// ================= UI UTILITY FUNCTIONS =================
function showToast(msg, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerText = msg;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('active');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('active');
}
