package com.intellidocs.intellidocs_ai.repository;

import com.intellidocs.intellidocs_ai.domain.entity.DocumentChunk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
        SELECT id, tenant_id, document_id, chunk_index,
                           content, page_number, token_count, created_at,
                           NULL::float4[] AS embedding,
                           search_vector
        FROM document_chunks
        WHERE tenant_id = CAST(:tenantId AS UUID)
        ORDER BY embedding <=> CAST(:queryVector As vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(UUID tenantId, String queryVector, int topK);

    @Modifying
    @Transactional
    @Query(value= """
        UPDATE document_chunks 
        SET search_vector = to_tsvector('english', content)
        WHERE id = :chunkId
        """, nativeQuery = true)
    void updateSearchVector(UUID chunkId);

     // Full-text search using the search_vector column
    @Query(value= """
        SELECT id, tenant_id, document_id, chunk_index, content, page_number, token_count, created_at, NULL::float4[] as embedding, search_vector
        FROM document_chunks
        WHERE tenant_Id = CAST (:tenantId As UUID)
        AND search_vector @@ plainto_tsquery('english', :query)
        ORDER BY ts_rank(search_vector,plainto_tsquery('english', :query)) DESC
        LIMIT :topK
        """, nativeQuery = true)
    List<DocumentChunk> findByFullTextSearch(@Param("tenantId")UUID tenantId,
                                             @Param("query") String query,
                                             @Param("topK") int topK);
}



