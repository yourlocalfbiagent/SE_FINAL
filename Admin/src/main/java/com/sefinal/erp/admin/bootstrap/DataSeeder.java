package com.sefinal.erp.admin.bootstrap;

import com.sefinal.erp.admin.model.Company;
import com.sefinal.erp.admin.model.Role;
import com.sefinal.erp.admin.model.User;
import com.sefinal.erp.admin.repository.CompanyRepository;
import com.sefinal.erp.admin.repository.RoleRepository;
import com.sefinal.erp.admin.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CompanyRepository companies;
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CompanyRepository companies, RoleRepository roles,
                      UserRepository users, PasswordEncoder passwordEncoder) {
        this.companies       = companies;
        this.roles           = roles;
        this.users           = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        users.findByEmail("admin@demo.local").ifPresent(u -> {
            if ("REPLACE_WITH_HASH".equals(u.getPasswordHash())) {
                users.updatePassword(u.getUserId(), passwordEncoder.encode("admin"));
                log.info("Repaired placeholder password hash for admin@demo.local");
            }
        });

        if (companies.count() > 0) return;

        Company demo = new Company();
        demo.setCompanyName("Demo Co.");
        demo.setCurrency("USD");
        demo.setTaxDefault(new BigDecimal("8.25"));
        demo.setLocale("en-US");
        demo.setActive(true);
        demo = companies.save(demo);

        Role admin = new Role();
        admin.setRoleName("Admin");
        admin.setDescription("Full access within the company");
        admin.setCompanyId(demo.getCompanyId());
        admin.setActive(true);
        admin = roles.save(admin);

        User user = new User();
        user.setFirstName("Demo");
        user.setLastName("Admin");
        user.setEmail("admin@demo.local");
        user.setPasswordHash(passwordEncoder.encode("admin"));
        user.setCompanyId(demo.getCompanyId());
        user.setRoleId(admin.getRoleId());
        user.setActive(true);
        user.setMfaEnabled(false);
        user.setFailedLoginAttempts(0);
        users.save(user);

        log.info("Seeded demo tenant: company={} role={} user=admin@demo.local (password: admin)",
                demo.getCompanyId(), admin.getRoleId());
    }
}
