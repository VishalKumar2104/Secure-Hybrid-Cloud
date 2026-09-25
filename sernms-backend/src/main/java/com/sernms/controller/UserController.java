package com.sernms.controller;

import com.sernms.dto.UserDto;
import com.sernms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Admin user provisioning, role assignments, and activations")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all user accounts")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user account by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle user active/inactive status")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(userService.toggleUserStatus(id, servletRequest));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Assign roles to user")
    public ResponseEntity<UserDto> assignRoles(@PathVariable Long id, @RequestBody Set<String> roles, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(userService.assignRoles(id, roles, servletRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user account")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, HttpServletRequest servletRequest) {
        userService.deleteUser(id, servletRequest);
        return ResponseEntity.ok("User deleted successfully");
    }
}
