package com.sefinal.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.sefinal.erp", "com.app.se_final_sales"})
@EnableJpaRepositories(basePackages = {"com.sefinal.erp", "com.app.se_final_sales"})
@EntityScan(basePackages = {"com.sefinal.erp", "com.app.se_final_sales"})
public class ErpApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }
}