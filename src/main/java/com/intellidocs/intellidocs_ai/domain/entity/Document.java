package com.intellidocs.intellidocs_ai.domain.entity;


import com.intellidocs.intellidocs_ai.domain.enums.DocumentStatus;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId; // Store tenantId directly for easy access, no @ManyToOne to Tenant

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy; // Store userId directly for easy access, no @ManyToOne to User

    @Column(nullable = false)
    private String title;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;


    @Column(name = "storage_key")
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING; // Default status is PENDING, can be PENDING, PROCESSED, FAILED

    @Column(name = "page_count")
    private Integer pageCount;

    // SHA-256 hash of file content — prevents duplicate uploads
    @Column(name = "content_hash", unique = true)
    private String contentHash;

    //Soft delete — we never hard delete documents
    @Column(name="deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
