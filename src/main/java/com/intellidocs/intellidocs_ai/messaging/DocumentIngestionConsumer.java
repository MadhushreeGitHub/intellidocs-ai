package com.intellidocs.intellidocs_ai.messaging;


import com.intellidocs.intellidocs_ai.domain.entity.Document;
import com.intellidocs.intellidocs_ai.domain.entity.DocumentChunk;
import com.intellidocs.intellidocs_ai.domain.enums.DocumentStatus;
import com.intellidocs.intellidocs_ai.repository.DocumentChunkRepository;
import com.intellidocs.intellidocs_ai.repository.DocumentRepository;
import com.intellidocs.intellidocs_ai.service.document.ChunkingService;
import com.intellidocs.intellidocs_ai.service.document.EmbeddingService;
import com.intellidocs.intellidocs_ai.service.document.TikaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

////the listener that ties everything together.
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentIngestionConsumer {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TikaService tikaService;
    private  final ChunkingService chunkService;
    private final EmbeddingService embeddingService;

    @RabbitListener(queues = "${app.messaging.document-ingestion-queue}")
    public void processDocument(DocumentIngestionMessage message){
        log.info("Received ingestion message for documentId={}, tenantId={}",
                message.getDocumentId(), message.getTenantId());

        //Load document - Confirm it is still exists
        Document doc = documentRepository.findById(message.getDocumentId())
                .orElseThrow(() -> new RuntimeException(
                        "Document not found: " + message.getDocumentId()));

        try{
            //step 1 - Mark as PROCESSING
            doc.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(doc);

            //Step 2 : Extract text with Apache Tika
            String text = tikaService.extractingText(message.getStoragePath());

            if(text.isBlank()){
                log.warn("No text extracted from Document {}", doc.getId());
                markfailed(doc, "No text extracted from document");
                return;
            }

            //Step 3 : Chunk text into smaller pieces
            List<ChunkingService.Chunk> chunks = chunkService.chunk(text);
            log.info("Document {} split into chunks", doc.getId());

            //Step 4 - Embed each chunk and save to DB
            for(ChunkingService.Chunk chunk : chunks) {
                float[] vector = embeddingService.embed(chunk.content());
                DocumentChunk saved = chunkRepository.save(
                        DocumentChunk.builder()
                                .tenantId(message.getTenantId())
                                .documentId(doc.getId())
                                .chunkIndex(chunk.index())
                                .content(chunk.content())
                                .embedding(vector)
                                .tokenCount(chunk.tokenCount())
                                .build()
                );
                // Populate tsvector after saving
                chunkRepository.updateSearchVector(saved.getId());
            }

            //Step 5 - Mark as READY
            doc.setStatus(DocumentStatus.READY);
            doc.setPageCount(chunks.size()); //approximate
            documentRepository.save(doc);

            log.info("Document {} ingestion complete with {} chunks", doc.getId(), chunks.size());

        }catch (Exception e){
            log.error("Ingestion failed for document {}: {}", doc.getId(), e.getMessage());
            markfailed(doc, e.getMessage());
            // Re-throw so RabbitMQ knows the message failed
            // After 3 failures, message goes to DLQ automatically
            throw new RuntimeException("Ingestion failed", e);
        }

    }

    private void markfailed(Document doc, String reason){
        doc.setStatus(DocumentStatus.FAILED);
        documentRepository.save(doc);
        log.error("Document {} marked as FAILED: {}", doc.getId(), reason);
    }

}
