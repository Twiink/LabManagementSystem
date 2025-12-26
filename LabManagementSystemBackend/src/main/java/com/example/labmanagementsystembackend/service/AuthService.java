package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.domain.entity.User;
import com.example.labmanagementsystembackend.dto.response.LoginResponse;
import com.example.labmanagementsystembackend.dto.response.UserResponse;
import com.example.labmanagementsystembackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {
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

    public LoginResponse login(String username, String rawPassword) {
        User user = userMapper.findByName(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "User not found");
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid credentials");
        }
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttlSeconds, ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(), user.getStatus());
        return new LoginResponse(token, "Bearer", ttlSeconds, response);
    }
}
