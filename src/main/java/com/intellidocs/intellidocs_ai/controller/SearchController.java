package com.intellidocs.intellidocs_ai.controller;

import com.intellidocs.intellidocs_ai.common.ApiResponse;
import com.intellidocs.intellidocs_ai.domain.entity.DocumentChunk;
import com.intellidocs.intellidocs_ai.domain.entity.Message;
import com.intellidocs.intellidocs_ai.dto.SearchResultDto;
import com.intellidocs.intellidocs_ai.service.document.EmbeddingService;
import com.intellidocs.intellidocs_ai.service.rag.ChatService;
import com.intellidocs.intellidocs_ai.service.rag.ConversationService;
import com.intellidocs.intellidocs_ai.service.rag.PromptBuilderService;
import com.intellidocs.intellidocs_ai.service.search.SearchService;
import com.intellidocs.intellidocs_ai.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final EmbeddingService embeddingService;
    private final ChatService chatService;
    // add to the field list at the top of SearchController:
    private final PromptBuilderService promptBuilderService;
    private final ConversationService conversationService;

    /// Lexical search — keyword matching
    @GetMapping("/lexical")
    public ResponseEntity<ApiResponse<List<DocumentChunk>>> lexical(@RequestParam String query) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        List<DocumentChunk> results = searchService.lexicalSearch(tenantId, query);
        return ResponseEntity.ok(ApiResponse.ok(results.size() + " results found", results));
    }

    ///Semantic search meaning based
    @GetMapping("/semantic")
    public ResponseEntity<ApiResponse<List<SearchResultDto>>> semantic(@RequestParam String query) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        // Embed the query using the same embedding method as documents
        float[] queryVector = embeddingService.embed(query);
        List<SearchResultDto> results =searchService.semanticSearch(tenantId,queryVector)
                .stream()
                .map(scored -> SearchResultDto.builder()
                        .chunkId(scored.chunk().getId())
                        .documentId(scored.chunk().getDocumentId())
                        .chunkIndex(scored.chunk().getChunkIndex())
                        .content(scored.chunk().getContent())
                        .score(Math.round(scored.score() * 10000.0) / 10000.0) // Round score for readability
                        .retrievalPath("Semantic")
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(results.size()
            + " semantic results found", results));
     }

     //// Hybrid — best of both worlds
    @GetMapping("/hybrid")
    public ResponseEntity<ApiResponse<List<SearchResultDto>>> hybrid(@RequestParam String query) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        List<SearchResultDto> results = searchService
                .hybridSearch(tenantId, query)
                .stream()
                .map(scored -> SearchResultDto.builder()
                        .chunkId(scored.chunk().getId())
                        .documentId(scored.chunk().getDocumentId())
                        .chunkIndex(scored.chunk().getChunkIndex())
                        .content(scored.chunk().getContent())
                        .score(Math.round(scored.score() * 10000.0) / 10000.0) // Round score for readability
                        .retrievalPath("hybrid")
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(results.size() + "hybrid results found", results));

    }

    //add this method - Day 11: preview the assembled RAG prompt (no LLM call yet)
    @GetMapping("/rag-preview")
    public ResponseEntity<ApiResponse<String>> ragPreview(@RequestParam String query,  @RequestParam(required = false) UUID conversationId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        float[] queryVector = embeddingService.embed(query);
        // Reuse Day 9's threshold-filtered semantic search as the context source
        var chunks = searchService.semanticSearch(tenantId, queryVector);
        // load recent history if continuing a conversation
        List<Message> history = (conversationId != null)
                ? conversationService.getRecentHistory(conversationId)
                : List.of();
        String prompt = promptBuilderService.buildPrompt(query, chunks, history);

        return ResponseEntity.ok(ApiResponse.ok(chunks.size() + " chunks used", prompt));

    }

    //Day 12: Full RAG - Retrive -> build prompt -> generate answer
    @GetMapping("/ask")
    public ResponseEntity<ApiResponse<String>> ask(
                                                    @RequestParam String query,
                                                     @RequestParam(required = false) UUID conversationId) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        UUID userId = UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()); // In real app, store userId in auth token and set in SecurityContext

        // load recent history if continuing a conversation
        List<Message> history = (conversationId != null)
                ? conversationService.getRecentHistory(conversationId)
                : List.of();

        //1. RETRIVE( threshold-filtered)
        float[] queryVector = embeddingService.embed(query);
        List<SearchService.ScoredChunk> retrievedChunks = searchService.semanticSearch(tenantId, queryVector);

        //2. AUGMENT (build prompt)
        String prompt = promptBuilderService.buildPrompt(query, retrievedChunks, history);

        //3. GENERATE(LLM answers)
        String answer = chatService.generateAnswer(prompt);


        UUID convId = conversationService.saveTurn(
                tenantId, userId, conversationId, query, answer, retrievedChunks);
        // return the answer AND the conversationId so the client can send follow-ups
        return ResponseEntity.ok(ApiResponse.ok("conversationId=" + convId, answer));
    }


}
