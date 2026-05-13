package com.sefinal.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.sefinal.erp", "com.app.se_final_sales"})
public class ErpApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }
}