package com.sernms.service;

import com.sernms.dto.AuthRequest;
import com.sernms.dto.AuthResponse;
import com.sernms.dto.ChangePasswordRequest;
import com.sernms.dto.RegisterRequest;
import com.sernms.dto.UserDto;
import com.sernms.entity.Role;
import com.sernms.entity.User;
import com.sernms.exception.BadRequestException;
import com.sernms.exception.ResourceNotFoundException;
import com.sernms.repository.RoleRepository;
import com.sernms.repository.UserRepository;
import com.sernms.security.JwtTokenProvider;
import com.sernms.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider, AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResponse login(AuthRequest request, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            auditLogService.log(user.getUsername(), "USER_LOGIN", "User", user.getId().toString(), clientIp, "SUCCESS", "User logged in successfully");

            return new AuthResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getDepartment(),
                    roles
            );
        } catch (Exception ex) {
            auditLogService.log(request.getUsername(), "USER_LOGIN_FAILED", "User", null, clientIp, "FAILURE", "Failed login attempt: " + ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public UserDto register(RegisterRequest request, HttpServletRequest servletRequest) {
        String clientIp = auditLogService.extractClientIp(servletRequest);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already in use");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getDepartment() != null ? request.getDepartment() : "General"
        );

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                String normalizedRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                Role role = roleRepository.findByName(normalizedRole)
                        .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));
                roles.add(role);
            }
        } else {
            Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new BadRequestException("Default role ROLE_EMPLOYEE not found"));
            roles.add(employeeRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        auditLogService.log(
                getCurrentUsername(),
                "USER_REGISTERED",
                "User",
                savedUser.getId().toString(),
                clientIp,
                "SUCCESS",
                "User account created for " + savedUser.getUsername()
        );

        return mapToDto(savedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, HttpServletRequest servletRequest) {
        String username = getCurrentUsername();
        String clientIp = auditLogService.extractClientIp(servletRequest);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            auditLogService.log(username, "PASSWORD_CHANGE_FAILED", "User", user.getId().toString(), clientIp, "FAILURE", "Incorrect current password");
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.log(username, "PASSWORD_CHANGED", "User", user.getId().toString(), clientIp, "SUCCESS", "Password updated successfully");
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentProfile() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return mapToDto(user);
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "ANONYMOUS";
        }
        return authentication.getName();
    }

    public User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
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
