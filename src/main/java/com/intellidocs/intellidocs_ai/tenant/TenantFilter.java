package com.intellidocs.intellidocs_ai.tenant;

//Short and focused. Reads the tenantId that JwtAuthFilter stashed on the request, puts it in TenantContext.

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {
    @Override
    protected  void doFilterInternal(HttpServletRequest request,
                                     @NotNull HttpServletResponse response,
                                     @NotNull FilterChain filterChain) throws ServletException, IOException{
        try {
            String tenantId = (String) request.getAttribute("tenantId");
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            // ALWAYS clear — finally block runs even if an exception occurs downstream
            TenantContext.clear();
        }
    }
}
