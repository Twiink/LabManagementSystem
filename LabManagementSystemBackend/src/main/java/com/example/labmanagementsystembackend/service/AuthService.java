package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.domain.entity.User;
import com.example.labmanagementsystembackend.dto.response.LoginResponse;
import com.example.labmanagementsystembackend.dto.response.UserResponse;
import com.example.labmanagementsystembackend.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long ttlSeconds;

    public AuthService(UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder,
                       @Value("${app.security.jwt.issuer}") String issuer,
                       @Value("${app.security.jwt.ttl-seconds}") long ttlSeconds) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
    }

    public LoginResponse login(String username, String rawPassword, String role) {
        log.info("Login attempt for user: {}, role: {}", username, role);

        User user = userMapper.findByName(username);
        if (user == null) {
            log.warn("User not found: {}", username);
            throw new BusinessException(ErrorCode.NOT_FOUND, "User not found");
        }

        log.info("User found: id={}, role={}, status={}", user.getId(), user.getRole(), user.getStatus());

        if (user.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Invalid credentials for user: {}", username);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid credentials");
        }

        // Verify role matches
        if (!user.getRole().equalsIgnoreCase(role)) {
            log.warn("Role mismatch for user: {}, expected: {}, got: {}", username, user.getRole(), role);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Role mismatch: please select correct identity");
        }

        log.info("Password verified, generating JWT token...");

        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttlSeconds, ChronoUnit.SECONDS);

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        log.info("JWT token generated successfully for user: {}", username);

        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(), user.getStatus());
        return new LoginResponse(token, "Bearer", ttlSeconds, response);
    }

    /**
     * Register a new student account
     * Only STUDENT role is allowed for self-registration
     */
    public UserResponse register(String username, String rawPassword, String email, String phone) {
        // Check if username already exists
        User existingUser = userMapper.findByName(username);
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Username already exists");
        }

        // Check if email already exists
        User existingEmail = userMapper.findByEmail(email);
        if (existingEmail != null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Email already registered");
        }

        // Create new student user
        User user = new User();
        user.setName(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole("STUDENT");  // Only allow student registration
        user.setStatus("ACTIVE");
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setIsDeleted(0);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        userMapper.insert(user);

        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(), user.getStatus());
    }
}
