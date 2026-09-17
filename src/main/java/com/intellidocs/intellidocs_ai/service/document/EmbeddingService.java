package com.intellidocs.intellidocs_ai.service.document;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

////Calls OpenAI and returns a vector for each chunk.

/**
 * Actual production code which call the OpenAI API to generate embeddings for document chunks. Spring AI's EmbeddingModel abstracts away the API call, so we just call embed() with our text and it returns the vector.
 * Make it uncomment for final use. For testing, we can mock this service to return dummy vectors without hitting the OpenAI API.
 */

/**
 *

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {
    // Spring AI auto-configures this bean from your application.yml
    // spring.ai.openai.api-key + embedding.options.model
    private final EmbeddingModel embeddingModel;

    public float[] embed(String text){
        try{
            // Spring AI handles the OpenAI API call
            // Returns 1536-dimension vector for ada-002 model
            float[] vector = embeddingModel.embed(text);
            // For simplicity, we return the first value of the vector.
            // In a real app, you'd store the entire vector in your DB.

            log.debug("Generated embedding for dimension: {}", vector.length);
            return vector;

        }catch(Exception e){
            log.error("Embedding failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate embedding");
        }
    }

    public List<float[]> embedBatch(List<String> texts){
      //Embed multiple texts - more efficient than one-by-one
        return texts.stream()
                .map(this::embed)
                .toList();
    }

} */

/**
 * Mock Embedding
 */



@Slf4j
@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(
            @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] embed(String text) {
        try {
            float[] vector = embeddingModel.embed(text);
            log.debug("Generated embedding — {} dimensions", vector.length);
            return vector;
        } catch (Exception e) {
            log.error("Embedding failed: {}", e.getMessage());
            throw new RuntimeException("Embedding generation failed", e);
        }
    }

    public List<float[]> embedBatch(List<String> texts) {
        return texts.stream()
                .map(this::embed)
                .toList();
    }
}