package com.sernms.controller;

import com.sernms.dto.AuthRequest;
import com.sernms.dto.AuthResponse;
import com.sernms.dto.ChangePasswordRequest;
import com.sernms.dto.RegisterRequest;
import com.sernms.dto.UserDto;
import com.sernms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for login, registration, and user session management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT bearer token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest) {
        AuthResponse response = authService.login(request, servletRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        UserDto userDto = authService.register(request, servletRequest);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/profile")
    @Operation(summary = "Retrieve current authenticated user profile")
    public ResponseEntity<UserDto> getCurrentProfile() {
        return ResponseEntity.ok(authService.getCurrentProfile());
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for the current user")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest servletRequest) {
        authService.changePassword(request, servletRequest);
        return ResponseEntity.ok("Password changed successfully");
    }
}
