package com.sernms.controller;

import com.sernms.dto.TopologyGraphDto;
import com.sernms.service.TopologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/topology")
@Tag(name = "Network Topology", description = "Endpoints for interactive visual network topology graph")
public class TopologyController {

    private final TopologyService topologyService;

    public TopologyController(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @GetMapping
    @Operation(summary = "Get full node-edge topology graph representing enterprise and hybrid cloud architecture")
    public ResponseEntity<TopologyGraphDto> getTopology() {
        return ResponseEntity.ok(topologyService.getTopologyGraph());
    }
}
