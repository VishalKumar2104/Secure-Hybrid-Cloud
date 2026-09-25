package com.sernms.dto;

import java.util.List;

public class TopologyGraphDto {
    private List<TopologyNodeDto> nodes;
    private List<TopologyEdgeDto> edges;
    private String hybridVpnStatus; // CONNECTED, DISCONNECTED, DEGRADED
    private int totalNodes;
    private int totalEdges;

    public TopologyGraphDto() {}

    public TopologyGraphDto(List<TopologyNodeDto> nodes, List<TopologyEdgeDto> edges, String hybridVpnStatus) {
        this.nodes = nodes;
        this.edges = edges;
        this.hybridVpnStatus = hybridVpnStatus;
        this.totalNodes = nodes != null ? nodes.size() : 0;
        this.totalEdges = edges != null ? edges.size() : 0;
    }

    public List<TopologyNodeDto> getNodes() { return nodes; }
    public void setNodes(List<TopologyNodeDto> nodes) { this.nodes = nodes; }

    public List<TopologyEdgeDto> getEdges() { return edges; }
    public void setEdges(List<TopologyEdgeDto> edges) { this.edges = edges; }

    public String getHybridVpnStatus() { return hybridVpnStatus; }
    public void setHybridVpnStatus(String hybridVpnStatus) { this.hybridVpnStatus = hybridVpnStatus; }

    public int getTotalNodes() { return totalNodes; }
    public void setTotalNodes(int totalNodes) { this.totalNodes = totalNodes; }

    public int getTotalEdges() { return totalEdges; }
    public void setTotalEdges(int totalEdges) { this.totalEdges = totalEdges; }
}
