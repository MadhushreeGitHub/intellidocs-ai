package com.intellidocs.intellidocs_ai.repository;

import com.intellidocs.intellidocs_ai.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<com.intellidocs.intellidocs_ai.domain.entity.User, Long> {

    Optional<User> findByEmail(String email);
     boolean existsByEmailAndTenantId(String email, UUID tenantId);
}
