package com.intellidocs.intellidocs_ai.dto;


import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SearchResultDto {
    private UUID chunkId;
    private UUID documentId;
    private int chunkIndex;
    private String content;
    private double score; //RRF score -higher = more relevant
    private  String retrievalPath; // "Semantic", "lexical" or "both"
}
