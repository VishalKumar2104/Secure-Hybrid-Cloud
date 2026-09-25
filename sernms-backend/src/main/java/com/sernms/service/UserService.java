package com.sernms.service;

import com.sernms.dto.UserDto;
import com.sernms.entity.Role;
import com.sernms.entity.User;
import com.sernms.exception.BadRequestException;
import com.sernms.exception.ResourceNotFoundException;
import com.sernms.repository.RoleRepository;
import com.sernms.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       AuditLogService auditLogService, AuthService authService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditLogService = auditLogService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    @Transactional
    public UserDto toggleUserStatus(Long id, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new BadRequestException("Root admin account cannot be deactivated");
        }

        user.setActive(!user.isActive());
        User updated = userRepository.save(user);

        auditLogService.log(
                authService.getCurrentUsername(),
                "USER_STATUS_TOGGLED",
                "User",
                user.getId().toString(),
                clientIp,
                "SUCCESS",
                "User " + user.getUsername() + " status changed to: " + (user.isActive() ? "ACTIVE" : "INACTIVE")
        );

        return mapToDto(updated);
    }

    @Transactional
    public UserDto assignRoles(Long id, Set<String> roleNames, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            String normalizedRole = name.startsWith("ROLE_") ? name : "ROLE_" + name;
            Role role = roleRepository.findByName(normalizedRole)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + name));
            roles.add(role);
        }

        user.setRoles(roles);
        User updated = userRepository.save(user);

        auditLogService.log(
                authService.getCurrentUsername(),
                "USER_ROLES_UPDATED",
                "User",
                user.getId().toString(),
                clientIp,
                "SUCCESS",
                "Assigned roles " + roleNames + " to user " + user.getUsername()
        );

        return mapToDto(updated);
    }

    @Transactional
    public void deleteUser(Long id, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new BadRequestException("Cannot delete default admin account");
        }

        userRepository.delete(user);

        auditLogService.log(
                authService.getCurrentUsername(),
                "USER_DELETED",
                "User",
                id.toString(),
                clientIp,
                "SUCCESS",
                "Deleted user account: " + user.getUsername()
        );
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDepartment(user.getDepartment());
        dto.setActive(user.isActive());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return dto;
    }
}
