package com.intellidocs.intellidocs_ai.messaging;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class DocumentIngestionMessage implements Serializable {
    private UUID documentId;
    private UUID tenantId;
    private String storagePath; // where the raw file is saved on disk
    private String originalName; // original file name, e.g. "report.pdf"
    private String fileType; // MIME type, e.g. "application/pdf"
}
