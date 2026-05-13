package com.sefinal.erp.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Auth is now handled by JwtAuthenticationFilter via SecurityConfig (SEC-903).
}
