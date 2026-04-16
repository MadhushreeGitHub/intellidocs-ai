package com.intellidocs.intellidocs_ai.config;

import com.intellidocs.intellidocs_ai.security.JwtAuthFilter;
import com.intellidocs.intellidocs_ai.tenant.TenantFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtAuthFilter jwtAuthFilter;
    private final TenantFilter tenantFilter;

    //public paths - no JWT required
    private  static final String[] PUBLIC_PATHS ={
            "/api/v1/auth/**",  //login, register, refresh
            "/actuator/health",  //load balancer health checks
            "/actuator/info",
            "/swagger-ui/**", //API docs and swagger UI should be public for easy testing - we can add auth later if needed
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    @Bean
    public  SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                //Disable CSRF - we Use JWT (stateless), CSRF only applies to stateful sessions with cookies
                .csrf(AbstractHttpConfigurer::disable)

                //Stateless session - Spring must NEVER create an HttpSession
                .sessionManagement(session ->
                        session.sessionCreationPolicy((SessionCreationPolicy.STATELESS)))

                //Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll() //allow public paths without auth
                        .anyRequest().authenticated() //all other requests require authentication
                ).addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
                        .class)
        //TenantFilter runs right after JwtAuthFilter
                .addFilterAfter(tenantFilter, JwtAuthFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                     //Triggered when no credentials provided -> should be 401
                     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                     response.setContentType("application/json");
                     response.getWriter().write(
                             "{\"Success\":false,\"Message\":\"Authentication Required\"}");
                 })
                .accessDeniedHandler(((request, response, accessDeniedException) -> {
                     response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                     response.setContentType("application/json");
                     response.getWriter().write(
                             "{\"Success\":false,\"Message\":\"Access Denied\"}"
                     );

                 })
                )
        );



        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // BCrypt with strength 12 — slow enough to resist brute force,
        // fast enough to not hurt UX (~300ms per hash on modern hardware)
        return new BCryptPasswordEncoder(12);
    }
}
