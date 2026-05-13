package com.sefinal.erp.admin.auth;

public record CurrentUser(int userId, int companyId, String email, Integer roleId) {}
