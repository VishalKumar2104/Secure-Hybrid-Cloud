package com.sernms;

import com.sernms.dto.*;
import com.sernms.entity.NetworkDevice;
import com.sernms.entity.User;
import com.sernms.repository.NetworkDeviceRepository;
import com.sernms.repository.UserRepository;
import com.sernms.security.JwtTokenProvider;
import com.sernms.security.UserPrincipal;
import com.sernms.service.AlertService;
import com.sernms.service.AwsService;
import com.sernms.service.DemoService;
import com.sernms.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class SernmsApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NetworkDeviceRepository deviceRepository;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AwsService awsService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DemoService demoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    @DisplayName("Test 1: Spring Boot Application Context & Default Data Seeding")
    void contextLoads() {
        assertTrue(userRepository.count() >= 4, "Users should be pre-seeded (admin, netadmin, cloudadmin, employee)");
        assertTrue(deviceRepository.count() >= 7, "Cisco campus network devices should be pre-seeded");
    }

    @Test
    @DisplayName("Test 2: Password Hashing with BCrypt")
    void testPasswordEncoding() {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertTrue(passwordEncoder.matches("Admin@123", admin.getPassword()), "BCrypt hash must verify against Admin@123");
    }

    @Test
    @DisplayName("Test 3: JWT Token Generation and Subject Extraction")
    void testJwtTokenGeneration() {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        UserPrincipal principal = UserPrincipal.create(admin);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String token = tokenProvider.generateToken(auth);
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("admin", tokenProvider.getUsernameFromJwt(token));
    }

    @Test
    @DisplayName("Test 4: AWS Dual-Mode Provider Resource Querying")
    void testAwsCloudProvider() {
        List<AwsResourceDto> resources = awsService.getAllResources();
        assertFalse(resources.isEmpty(), "AWS resources list should not be empty");

        boolean hasEc2 = resources.stream().anyMatch(r -> "EC2".equalsIgnoreCase(r.getResourceType()));
        boolean hasRds = resources.stream().anyMatch(r -> "RDS".equalsIgnoreCase(r.getResourceType()));
        boolean hasVgw = resources.stream().anyMatch(r -> "VGW".equalsIgnoreCase(r.getResourceType()));

        assertTrue(hasEc2, "Should include EC2 instances");
        assertTrue(hasRds, "Should include RDS databases");
        assertTrue(hasVgw, "Should include IPSec Virtual Private Gateway");
    }

    @Test
    @Transactional
    @DisplayName("Test 5: Device Status Transition triggers Alert")
    void testDeviceFailureAlertTrigger() {
        NetworkDevice device = deviceRepository.findByName("R-EDGE-01").orElseThrow();
        MockHttpServletRequest request = new MockHttpServletRequest();

        deviceService.updateDeviceStatus(device.getId(), "OFFLINE", request);

        NetworkDevice updated = deviceRepository.findById(device.getId()).orElseThrow();
        assertEquals("OFFLINE", updated.getStatus());

        List<AlertDto> activeAlerts = alertService.getActiveAlerts();
        boolean alertFound = activeAlerts.stream().anyMatch(a -> a.getAlertTitle().contains("R-EDGE-01"));
        assertTrue(alertFound, "Offline status change must trigger a Critical alert");
    }

    @Test
    @Transactional
    @DisplayName("Test 6: Controlled Demo Scenario (High CPU Spike & VPN Failover)")
    void testDemoScenarios() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        // 1. EC2 High CPU simulation
        DemoSimulationRequest simCpu = new DemoSimulationRequest();
        simCpu.setScenario("HIGH_CPU");
        simCpu.setTargetId("i-07f9c81a2b");
        simCpu.setCpuValue(95.5);

        Map<String, Object> cpuRes = demoService.executeScenario(simCpu, request);
        assertEquals("SUCCESS", cpuRes.get("status"));

        // 2. IPSec VPN failure simulation
        DemoSimulationRequest simVpn = new DemoSimulationRequest();
        simVpn.setScenario("VPN_FAIL");
        Map<String, Object> vpnRes = demoService.executeScenario(simVpn, request);
        assertEquals("SUCCESS", vpnRes.get("status"));
        assertEquals("DISCONNECTED", awsService.getVpnStatus());
    }
}
