package com.intellidocs.intellidocs_ai.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * This maps to the document_chunks table Flyway already created. The embedding column is type vector(1536) in Postgres —
 *  need the pgvector JDBC type to handle it.
 */

@Entity
@Table(name = "document_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false )
    private UUID tenantId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    //The actual text of this chunk
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 1536-dimension vector from OpenAI ada-002
    // Stored as pgvector type — requires pgvector JDBC extension
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "token_count")
    private Integer tokenCount;

    @CreationTimestamp
    private Instant createdAt;

}
