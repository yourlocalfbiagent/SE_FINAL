package com.sefinal.erp.admin.repository;

import com.sefinal.erp.admin.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {}
