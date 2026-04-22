package com.intellidocs.intellidocs_ai.controller;

import com.intellidocs.intellidocs_ai.common.ApiResponse;
import com.intellidocs.intellidocs_ai.domain.entity.Document;
import com.intellidocs.intellidocs_ai.service.document.DocumentService;
import com.intellidocs.intellidocs_ai.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Document>> upload(
            @RequestParam("file") MultipartFile file) {

        // TenantContext has the tenantId — set by TenantFilter
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        UUID userId = UUID.fromString(SecurityContextHolder.getContext()
                .getAuthentication()
                .getName()); // In real app, store userId in auth token and set in SecurityContext
        Document doc = documentService.upload(file, tenantId, userId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)   // 202 — processing async
                .body(ApiResponse.ok("Document uploaded. Processing started.", doc));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Document>>> list(Pageable pageable) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        Page<Document> docs = documentService.listDocuments(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(docs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Document>> getById(@PathVariable UUID id) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        Document doc = documentService.getDocument(id, tenantId);
        return ResponseEntity.ok(ApiResponse.ok(doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        documentService.softDelete(id, tenantId);
        return ResponseEntity.ok(ApiResponse.ok("Document deleted", null));
    }
}