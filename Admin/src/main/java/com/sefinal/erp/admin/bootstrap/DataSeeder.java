package com.sefinal.erp.admin.bootstrap;

import com.sefinal.erp.admin.dao.CompanyDao;
import com.sefinal.erp.admin.dao.RoleDao;
import com.sefinal.erp.admin.dao.UserDao;
import com.sefinal.erp.admin.model.Company;
import com.sefinal.erp.admin.model.Role;
import com.sefinal.erp.admin.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CompanyDao companies;
    private final RoleDao roles;
    private final UserDao users;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CompanyDao companies, RoleDao roles, UserDao users, PasswordEncoder passwordEncoder) {
        this.companies = companies;
        this.roles = roles;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Repair any user whose hash was never set (can happen from a previous broken seed run).
        users.findByEmail("admin@demo.local").ifPresent(u -> {
            if ("REPLACE_WITH_HASH".equals(u.passwordHash())) {
                users.updatePassword(u.userId(), passwordEncoder.encode("admin"));
                log.info("Repaired placeholder password hash for admin@demo.local");
            }
        });

        if (!companies.findAll().isEmpty()) return;

        Company demo = companies.create(new Company(
                null, "Demo Co.", "USD", new BigDecimal("8.25"),
                "en-US", true, null));
        Role admin = roles.create(new Role(
                null, "Admin", "Full access within the company",
                demo.companyId(), true));
        users.create(new User(
                null, "Demo", "Admin", "admin@demo.local",
                passwordEncoder.encode("admin"),
                demo.companyId(), admin.roleId(),
                true, false, 0, null, null));

        log.info("Seeded demo tenant: company={} role={} user=admin@demo.local (password: admin)",
                demo.companyId(), admin.roleId());
    }
}
