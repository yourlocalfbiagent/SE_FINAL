package com.sefinal.erp.admin.repository;

import com.sefinal.erp.admin.model.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSpec {

    private AuditLogSpec() {}

    public static Specification<AuditLog> filter(
            int companyId, Integer userId, String action,
            String entityType, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("companyId"), companyId));
            if (userId != null)
                preds.add(cb.equal(root.get("userId"), userId));
            if (action != null && !action.isBlank())
                preds.add(cb.like(root.get("action"), action + "%"));
            if (entityType != null && !entityType.isBlank())
                preds.add(cb.equal(root.get("entityType"), entityType));
            if (from != null)
                preds.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            if (to != null)
                preds.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }
}
