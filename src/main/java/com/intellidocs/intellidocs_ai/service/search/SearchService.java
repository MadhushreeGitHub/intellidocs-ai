package com.intellidocs.intellidocs_ai.service.search;


import com.intellidocs.intellidocs_ai.domain.entity.DocumentChunk;
import com.intellidocs.intellidocs_ai.repository.DocumentChunkRepository;

import com.intellidocs.intellidocs_ai.service.document.EmbeddingService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Value("${app.rag.similarity-threshold:0.70}")
    private double similarityThreshold;

    // Lexical search — BM25 equivalent via PostgreSQL full-text search
    // Best for: exact keywords, proper nouns, contract clause numbers
    public List<DocumentChunk> lexicalSearch(UUID tenantId, String query) {
        log.info("Performing lexical search for tenant {} with query: {}", tenantId, query);
        return chunkRepository.findByFullTextSearch(tenantId, query, topK);
    }

    // Semantic search — pgvector cosine similarity
    // Best for: meaning-based queries, synonyms, paraphrasing
    // NOTE: queryVector must be a real embedding — works with mock too
    public List<ScoredChunk> semanticSearch(UUID tenantId, float[] queryVector){
        log.info("Semantic search for tenant {} (threshold={})", tenantId, similarityThreshold);
        String vectorString = toVectorString(queryVector);

        List<Object[]> rows = chunkRepository.findSimilarChunksScored(tenantId,vectorString, topK);
        List<ScoredChunk> kept = new ArrayList<>();

        for(Object[] row : rows){
            ScoredChunk scoredChunk = mapRowToScoredChunk(row);
            String preview = scoredChunk.chunk.getContent()
                    .substring(0,Math.min(45,scoredChunk.chunk().getContent().length()));
            if(scoredChunk.score() >= similarityThreshold){
                log.info("Keep chunk {} (score={}) preview='{}'", scoredChunk.chunk().getId(), scoredChunk.score(), preview);
                kept.add(scoredChunk);
            }else {
                log.info("Discard chunk {} (score={}) preview='{}'", scoredChunk.chunk().getId(), scoredChunk.score(), preview);
            }

        }

        log.info("Semantic search: {} of {} chunks passed threshold {}",
                kept.size(), rows.size(), similarityThreshold);
        return kept;
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
        List<ScoredChunk> semanticResults = semanticSearch(
                tenantId, embeddingService.embed(query));
        List<DocumentChunk> lexicalResults = lexicalSearch(tenantId, query);

        //RRF Constant - standard value is 60
        final int K = 60;

        //Map Chunk ID -> RRF score
        Map<UUID, Double> scores = new HashMap<>();
        Map<UUID, DocumentChunk> chunks = new HashMap<>();

        //Score semantic results - rant starts at 1
        for (int i = 0; i  < semanticResults.size(); i++){
            DocumentChunk chunk = semanticResults.get(i).chunk();
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

    ///map the Object[] row to ScoredChunk
    private ScoredChunk mapRowToScoredChunk(Object[] row){
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId((UUID) row[0]);
        chunk.setTenantId((UUID) row[1]);
        chunk.setDocumentId((UUID) row[2]);
        chunk.setChunkIndex((Integer) row[3]);
        chunk.setContent((String) row[4]);
        chunk.setPageNumber((Integer) row[5]);
        chunk.setTokenCount((Integer) row[6]);
        double similarity = ((Number) row[10]).doubleValue();
        return new ScoredChunk(chunk, similarity); // Return a ScoredChunk with the actual similarity score
    }




}
