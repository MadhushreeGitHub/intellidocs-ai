package com.intellidocs.intellidocs_ai.controller;

import com.intellidocs.intellidocs_ai.common.ApiResponse;
import com.intellidocs.intellidocs_ai.domain.entity.DocumentChunk;
import com.intellidocs.intellidocs_ai.service.document.EmbeddingService;
import com.intellidocs.intellidocs_ai.service.search.SearchService;
import com.intellidocs.intellidocs_ai.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // Lexical search — keyword matching
    @GetMapping("/lexical")
    public ResponseEntity<ApiResponse<List<DocumentChunk>>> lexical(@RequestParam String query) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        List<DocumentChunk> results = searchService.lexicalSearch(tenantId, query);
        return ResponseEntity.ok(ApiResponse.ok(results.size() + " results found", results));
    }

    //Semantic search meaning based
    @GetMapping("/semantic")
    public ResponseEntity<ApiResponse<List<DocumentChunk>>> semantic(@RequestParam String query) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        // Embed the query using the same embedding method as documents
        float[] queryVector = embeddingService.embed(query);
        List<DocumentChunk> results = searchService.semanticSearch(tenantId, queryVector);
        return ResponseEntity.ok(ApiResponse.ok(results.size() + " results found", results));
     }
}
