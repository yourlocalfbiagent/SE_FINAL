package com.sefinal.erp.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
public class AdminClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestTemplate restTemplate;
    private final String adminBaseUrl;

    public AdminClient(RestTemplate restTemplate,
                       @Value("${app.admin.base-url:http://localhost:8081}") String adminBaseUrl) {
        this.restTemplate = restTemplate;
        this.adminBaseUrl = adminBaseUrl;
    }

    /** Validate an ADMIN_SESSION cookie — returns current user info or empty if invalid. */
    public Optional<Map<String, Object>> validateSession(String sessionCookie) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    adminBaseUrl + "/api/auth/me",
                    HttpMethod.GET,
                    withSession(sessionCookie),
                    MAP_TYPE
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.Unauthorized e) {
            return Optional.empty();
        }
    }

    /** Look up a user by ID from the Admin service. */
    public Optional<Map<String, Object>> getUserById(long userId, String sessionCookie) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    adminBaseUrl + "/api/users/" + userId,
                    HttpMethod.GET,
                    withSession(sessionCookie),
                    MAP_TYPE
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    /** Look up a user by email from the Admin service. */
    public Optional<Map<String, Object>> getUserByEmail(String email, String sessionCookie) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    adminBaseUrl + "/api/users?email=" + email,
                    HttpMethod.GET,
                    withSession(sessionCookie),
                    MAP_TYPE
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    private static HttpEntity<Void> withSession(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "ADMIN_SESSION=" + sessionCookie);
        return new HttpEntity<>(headers);
    }
}
