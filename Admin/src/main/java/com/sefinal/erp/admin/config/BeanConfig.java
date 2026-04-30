package com.sefinal.erp.admin.config;

import com.sefinal.erp.admin.dao.ActionDao;
import com.sefinal.erp.admin.dao.AuditDao;
import com.sefinal.erp.admin.dao.CompanyDao;
import com.sefinal.erp.admin.dao.ModuleDao;
import com.sefinal.erp.admin.dao.PermissionDao;
import com.sefinal.erp.admin.dao.RoleDao;
import com.sefinal.erp.admin.dao.UserDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class BeanConfig {

    @Bean public CompanyDao    companyDao(DataSource ds)    { return new CompanyDao(ds); }
    @Bean public RoleDao       roleDao(DataSource ds)       { return new RoleDao(ds); }
    @Bean public UserDao       userDao(DataSource ds)       { return new UserDao(ds); }
    @Bean public ModuleDao     moduleDao(DataSource ds)     { return new ModuleDao(ds); }
    @Bean public ActionDao     actionDao(DataSource ds)     { return new ActionDao(ds); }
    @Bean public PermissionDao permissionDao(DataSource ds) { return new PermissionDao(ds); }
    @Bean public AuditDao      auditDao(DataSource ds)      { return new AuditDao(ds); }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
