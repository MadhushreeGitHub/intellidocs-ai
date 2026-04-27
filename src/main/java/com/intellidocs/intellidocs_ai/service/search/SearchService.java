package com.intellidocs.intellidocs_ai.service.search;


import com.intellidocs.intellidocs_ai.domain.entity.DocumentChunk;
import com.intellidocs.intellidocs_ai.repository.DocumentChunkRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final DocumentChunkRepository chunkRepository;

    @Value("${app.rag.top-k:5}")
    private int topK;

    // Lexical search — BM25 equivalent via PostgreSQL full-text search
    // Best for: exact keywords, proper nouns, contract clause numbers
    public List<DocumentChunk> lexicalSearch(UUID tenantId, String query) {
        log.info("Performing lexical search for tenant {} with query: {}", tenantId, query);
        return chunkRepository.findByFullTextSearch(tenantId, query, topK);
    }

    // Semantic search — pgvector cosine similarity
    // Best for: meaning-based queries, synonyms, paraphrasing
    // NOTE: queryVector must be a real embedding — works with mock too
    public List<DocumentChunk> semanticSearch(UUID tenantId, float[] queryVector){
        log.info("Performing semantic search for tenant {} with query vector of length {}", tenantId, queryVector.length);
        String VectorString = toVectorString(queryVector);
        return chunkRepository.findSimilarChunks(tenantId, VectorString, topK);
    }

    //Convert the float[] query vector to a string format that can be used in the native SQL query for pgvector similarity search. The format should be like: '[0.1, 0.2, 0.3, ...]'
    private String toVectorString(float[] queryVector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0 ; i  < queryVector.length; i++){
            sb.append(queryVector[i]);
            if (i < queryVector.length - 1){
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }



}
