package com.intellidocs.intellidocs_ai.service.document;

import com.intellidocs.intellidocs_ai.config.RabbitMQConfig;
import com.intellidocs.intellidocs_ai.domain.entity.Document;
import com.intellidocs.intellidocs_ai.domain.enums.DocumentStatus;
import com.intellidocs.intellidocs_ai.messaging.DocumentIngestionMessage;
import com.intellidocs.intellidocs_ai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.storage.local-path}")
    private String storagePath;

    public Document upload(MultipartFile file, UUID tenantId, UUID userId) {
        try {
            // 1. Hash file content — detect duplicates
            byte[] bytes = file.getBytes();
            String hash = sha256(bytes);

            // 2. Check for duplicate
            if (documentRepository.findByContentHash(hash).isPresent()) {
                throw new IllegalStateException(
                        "This document has already been uploaded");
            }

            // 3. Save file to local storage
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(storagePath, tenantId.toString());
            Files.createDirectories(targetPath);
            Path filePath = targetPath.resolve(fileName);
            Files.write(filePath, bytes);

            // 4. Create DB record with PENDING status
            Document doc = documentRepository.save(
                    Document.builder()
                            .tenantId(tenantId)
                            .uploadedBy(userId) // placeholder — in real app, use actual userId from auth context
                            .title(file.getOriginalFilename())
                            .fileName(file.getOriginalFilename())
                            .fileType(getExtension(file.getOriginalFilename()))
                            .fileSizeBytes(file.getSize())
                            .storageKey(filePath.toString())
                            .contentHash(hash)
                            .status(DocumentStatus.PENDING)
                            .build()
            );

            // 5. Publish to RabbitMQ — processing happens async
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INGESTION_EXCHANGE,
                    RabbitMQConfig.INGESTION_QUEUE,
                    DocumentIngestionMessage.builder()
                            .documentId(doc.getId())
                            .tenantId(tenantId)
                            .storagePath(filePath.toString())
                            .fileType(doc.getFileType())
                            .originalName(doc.getFileName())
                            .build()
            );

            log.info("Document {} queued for ingestion", doc.getId());
            return doc;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    public Page<Document> listDocuments(UUID tenantId, Pageable pageable) {
        return documentRepository.findByTenantId(tenantId, pageable);
    }

    public Document getDocument(UUID id, UUID tenantId) {
        return documentRepository.findById(id)
                .filter(doc -> doc.getTenantId().equals(tenantId)) // tenant check
                .filter(doc -> doc.getDeletedAt() == null)          // not deleted
                .orElseThrow(() ->
                        new RuntimeException("Document not found"));
    }

    public void softDelete(UUID id, UUID tenantId) {
        Document doc = getDocument(id, tenantId);
        doc.setDeletedAt(Instant.now());
        documentRepository.save(doc);
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (Exception e) {
            throw new RuntimeException("Hash failed", e);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "unknown";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}