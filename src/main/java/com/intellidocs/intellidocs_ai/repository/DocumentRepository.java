package com.intellidocs.intellidocs_ai.repository;

import com.intellidocs.intellidocs_ai.domain.entity.Document;
import com.intellidocs.intellidocs_ai.domain.enums.DocumentStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    // All docs for a tenant — excludes soft-deleted
    @Query("SELECT d FROM Document d " +
            "WHERE d.tenantId = :tenantId " +
            "AND d.deletedAt IS NULL " +
            "ORDER BY d.createdAt DESC")
    Page<Document> findByTenantId(UUID tenantId, Pageable pageable);


    // Check for duplicate upload by content hash
    Optional<Document> findByContentHash(String contentHash);


    // Find docs stuck in PROCESSING (for retry job later)
    @Query(value = "SELECT d FROM Document d " +
            "WHERE d.tenantId = :tenantId " +
            "AND d.status = :status " +
            "AND d.deletedAt IS NULL")
    Page<Document> findByTenantIdAndStatus(
            UUID tenantId, DocumentStatus status, Pageable pageable
    );
}
