package com.intellidocs.intellidocs_ai.domain.entity;

import com.intellidocs.intellidocs_ai.domain.enums.UserRole;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    // tenant_id FK — we store the UUID directly, not a @ManyToOne
    // Reason: To avoid lazy-loading a Tenant just to check tenant
    @Column(unique = true, nullable = false)
    private UUID tenantId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hashed, never plain text

    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER; // Default role is USER, can be ADMIN


    @CreationTimestamp
    private Instant createdAt;


    @UpdateTimestamp
    private Instant updatedAt;
}
