package com.sefinal.erp.admin.web.dto;

import java.math.BigDecimal;

public final class Dtos {

    private Dtos() {}

    public record LoginRequest(String email, String password) {}

    public record UpdatePasswordRequest(String newPassword) {}

    public record CreateCompanyRequest(
            String companyName,
            String currency,
            BigDecimal taxDefault,
            String locale,
            Boolean isActive
    ) {}

    public record CreateRoleRequest(
            String roleName,
            String description,
            Boolean isActive
    ) {}

    public record CreateUserRequest(
            String firstName,
            String lastName,
            String email,
            String password,
            Integer roleId,
            Boolean isActive,
            Boolean mfaEnabled
    ) {}
}
