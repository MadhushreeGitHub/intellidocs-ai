package com.intellidocs.intellidocs_ai.service.document;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


////Splits extracted text into overlapping chunks so no sentence is cut off at a boundary.
@Slf4j
@Service
public class ChunkingService {
    @Value("${app.rag.chuk-size:512}")
    private  int chunkSize; // tokens per chunk

    @Value("${app.rag.chunk-loverlap:50}")
    private  int chunkOverlap; // overlap between adjacent chunks

    public record Chunk(String content, int index, int tokenCount) {}

    public List<Chunk> chunk(String text){
        if(text == null || text.isBlank()){
            return List.of();
        }

        // Simple word-based tokenization
        // In production you'd use tiktoken for accurate token counts

        String[] words = text.split("\\s+");
        List<Chunk> chunks = new java.util.ArrayList<>();
        int start = 0;
        int index = 0;

        while (start < words.length ){
            int end = Math.min(start + chunkSize, words.length);

            //join words back in to text
            String chunkText = String.join(" ",
                    java.util.Arrays.copyOfRange(words, start, end));
            chunks.add(new Chunk(chunkText, index++, end - start));

            // Move start forward by (chunkSize - overlap)
            // This creates the sliding window overlap
            start += (chunkSize - chunkOverlap);

            // Prevent infinite loop if chunkOverlap >= chunkSize
            if (chunkSize <= chunkOverlap) break;

        }
        log.info("Split text into {} chunks ({} words total)",
                chunks.size(), words.length);
        return chunks;
    }

}
