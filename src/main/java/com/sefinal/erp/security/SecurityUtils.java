package com.sefinal.erp.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCompanyId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Claims claims = (Claims) principal;
        return ((Number) claims.get("companyId")).longValue();
    }
}
