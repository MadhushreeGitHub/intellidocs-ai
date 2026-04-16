package com.intellidocs.intellidocs_ai.domain.enums;

public enum DocumentStatus {
    PENDING, //just uploaded, queued for processing
    PROCESSING, //Tika extraction + embedding in progress
    READY, // fully indexed, available for search
    FAILED //Processing failed - error stored on document
}
