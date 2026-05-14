package com.sefinal.erp.admin.security;

import com.sefinal.erp.admin.auth.CurrentUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.isValid(token)) {
                Claims claims = jwtTokenProvider.extractClaims(token);
                int userId = ((Number) claims.get("userId")).intValue();
                int companyId = ((Number) claims.get("companyId")).intValue();
                String email = claims.getSubject();
                Object roleIdObj = claims.get("roleId");
                Integer roleId = roleIdObj != null ? ((Number) roleIdObj).intValue() : null;
                String role = claims.get("role", String.class);

                CurrentUser principal = new CurrentUser(userId, companyId, email, roleId);
                List<SimpleGrantedAuthority> authorities = role != null
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        : List.of();
                var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
