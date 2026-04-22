package com.intellidocs.intellidocs_ai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

//This runs on every single request. It reads the JWT, validates it, and tells Spring Security who the user is.

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private  final JwtUtil jwtUtil;

    @Override
    protected  void doFilterInternal(HttpServletRequest request,
                                     @NotNull HttpServletResponse response,
                                     @NotNull FilterChain filterChain)  throws ServletException, IOException{

        String authHeader = request.getHeader("Authorization");
        System.out.println("=== AUTH HEADER: " + request.getHeader("Authorization"));

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            //No token - just continue without setting authentication
            filterChain.doFilter(request, response);
            return;
        }

        String token =authHeader.substring(7); //strip "Bearer "

        try{
            Claims claims = jwtUtil.validateAndExtract(token);
            String userId = jwtUtil.extractUserId(claims);
            String tenantId = jwtUtil.extractTenantId(claims);
            String role = jwtUtil.extractRole(claims);

            //Tell Spring Security: This user is authenticated with the this role

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);

            //Search tenantId on the request for TenantFilter to pick up and associate with the request
            request.setAttribute("tenantId", tenantId);
        }catch (JwtException e){
            //Invalid or expired token - Could log this if needed, but just continue chain without setting authentication
            //Spring Security's AuthorizationFilter will then reject the request with 401
            SecurityContextHolder.clearContext();

        }
        filterChain.doFilter(request, response);
    }

}
