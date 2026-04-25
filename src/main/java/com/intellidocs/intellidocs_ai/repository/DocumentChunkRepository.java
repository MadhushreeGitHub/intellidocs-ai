package com.intellidocs.intellidocs_ai.repository;

import com.intellidocs.intellidocs_ai.domain.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    // Delete all chunks when a document is deleted
    @Modifying
    @Query("DELETE FROM DocumentChunk c " +
            "WHERE c.documentId = :documentId")
    void deleteByDocumentId(UUID documentId);


    //// Count chunks for a document — useful for status checks
    long countByDocumentId(UUID documentId);

    // Semantic search — find top-K most similar chunks for a query vector
    // Raw SQL needed because JPQL doesn't support pgvector operators
    @Query(value= """
        SELECT * FROM document_chunks
        WHERE tenant_id = :tenantId
        ORDER BY embedding <=> CAST(:queryVector As vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(UUID tenantId, String queryVector, int topK);

}



