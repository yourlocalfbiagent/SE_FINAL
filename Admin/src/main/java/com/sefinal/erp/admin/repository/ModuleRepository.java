package com.sefinal.erp.admin.repository;

import com.sefinal.erp.admin.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Integer> {
    Optional<Module> findByModuleName(String moduleName);
}
