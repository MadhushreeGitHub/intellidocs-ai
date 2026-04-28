package com.intellidocs.intellidocs_ai.service.search;


import com.intellidocs.intellidocs_ai.domain.entity.DocumentChunk;
import com.intellidocs.intellidocs_ai.repository.DocumentChunkRepository;

import com.intellidocs.intellidocs_ai.service.document.EmbeddingService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    // inner record for search results with scores
    public record ScoredChunk(DocumentChunk chunk, double score) {}

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

    //method to SearchService:
    public List<ScoredChunk> hybridSearch(UUID tenantId, String query) {
        log.info("Performing hybrid search for tenant {} with query: {}", tenantId, query);

        //Run both searches in parallel - get ranked lists
        List<DocumentChunk> semanticResults = semanticSearch(
                tenantId, embeddingService.embed(query));
        List<DocumentChunk> lexicalResults = lexicalSearch(tenantId, query);

        //RRF Constant - standard value is 60
        final int K = 60;

        //Map Chunk ID -> RRF score
        Map<UUID, Double> scores = new HashMap<>();
        Map<UUID, DocumentChunk> chunks = new HashMap<>();

        //Score semantic results - rant starts at 1
        for (int i = 0; i  < semanticResults.size(); i++){
            DocumentChunk chunk = semanticResults.get(i);
            double rrfScore = 1.0/(i + 1 + K);
            scores.merge(chunk.getId(), rrfScore, Double::sum);
            chunks.put(chunk.getId(), chunk);
        }

        //Score lexical results - add to existing scores
        for (int i = 0; i  < lexicalResults.size(); i++){
            DocumentChunk chunk = lexicalResults.get(i);
            double rrfScore = 1.0/(i + 1 + K);
            scores.merge(chunk.getId(), rrfScore, Double::sum);
            chunks.put(chunk.getId(), chunk);
        }

        //Sort by RRF Score descending -highest score first
        return scores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .map(entry -> new ScoredChunk(chunks.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());

    }




}
