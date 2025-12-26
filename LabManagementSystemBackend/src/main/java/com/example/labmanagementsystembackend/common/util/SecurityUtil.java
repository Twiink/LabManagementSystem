package com.example.labmanagementsystembackend.common.util;

import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtil {
    private SecurityUtil() {
    }

    public static Long getUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            return null;
        }
        return Long.valueOf(jwt.getSubject());
    }

    public static String getRole(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        return jwt.getClaimAsString("role");
    }
}
