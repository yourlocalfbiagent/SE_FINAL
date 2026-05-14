package com.sefinal.erp.admin.repository;

import com.sefinal.erp.admin.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActionRepository extends JpaRepository<Action, Integer> {
    Optional<Action> findByActionName(String actionName);
}
