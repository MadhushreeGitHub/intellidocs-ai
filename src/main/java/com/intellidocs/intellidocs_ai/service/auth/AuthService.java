package com.intellidocs.intellidocs_ai.service.auth;

import com.intellidocs.intellidocs_ai.domain.entity.Tenant;
import com.intellidocs.intellidocs_ai.domain.entity.User;
import com.intellidocs.intellidocs_ai.dto.request.LoginRequest;
import com.intellidocs.intellidocs_ai.dto.request.RegisterRequest;
import com.intellidocs.intellidocs_ai.dto.response.AuthResponse;
import com.intellidocs.intellidocs_ai.repository.TenantRepository;
import com.intellidocs.intellidocs_ai.repository.UserRepository;
import com.intellidocs.intellidocs_ai.security.JwtUtil;
import com.rabbitmq.client.Return;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request){
        // 1. Create slug from company name e.g. "Acme Corp" → "acme-corp"
        String slug = request.getCompanyName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+","-")
                .replaceAll("^-|-$",""); // remove leading/trailing hyphens

        //Make slug unique if it already exists
        if(tenantRepository.existsBySlug(slug)){
            slug = slug + "-" + System.currentTimeMillis();
        }

        //2. Create tenant
        Tenant tenant = tenantRepository.save(
                Tenant.builder()
                        .name(request.getCompanyName())
                        .slug(slug)
                        .build()
        );

        //3. Create user under that tenant
        User user = userRepository.save(
                User.builder()
                        .fullName(request.getFullName())
                        .email(request.getEmail())
                        // NEVER store plain text — always hash passwords using a strong algorithm like BCrypt
                        .password(passwordEncoder.encode(request.getPassword()))
                        .tenantId(tenant.getId())
                        .build()
        );


        //4. Generate JWT with tenantID embedded
        String token = jwtUtil.generateToken(
                user.getId().toString(),
                tenant.getId().toString(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours in milliseconds
                .userId(user.getId().toString())
                .tenantId(tenant.getId().toString())
                .role(user.getRole().name())
                .build();


    }

    public AuthResponse login(LoginRequest request){

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Verify password against BCrypt hash
        // NEVER compare plain text — always use passwordEncoder.matches()
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Generate JWT with tenantID embedded
        String token = jwtUtil.generateToken(
                user.getId().toString(),
                user.getTenantId().toString(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours in milliseconds
                .userId(user.getId().toString())
                .tenantId(user.getTenantId().toString())
                .role(user.getRole().name())
                .build();



    }
}
