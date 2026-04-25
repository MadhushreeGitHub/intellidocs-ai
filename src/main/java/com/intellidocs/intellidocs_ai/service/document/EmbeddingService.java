package com.intellidocs.intellidocs_ai.service.document;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
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

    private static final int DIMENSION = 1536;

    public float[] embed(String text) {
        // MOCK — generates deterministic vector from text hashcode
        // Same text always produces same vector — consistent but not semantic
        // Replace with real EmbeddingModel when OpenAI credits are added
        float[] vector = new float[DIMENSION];
        Random random = new Random(text.hashCode());
        float magnitude = 0f;

        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = random.nextFloat() * 2 - 1; // range [-1, 1]
            magnitude += vector[i] * vector[i];
        }

        // Normalize to unit vector — same as real embeddings
        // Cosine similarity only works correctly with unit vectors
        magnitude = (float) Math.sqrt(magnitude);
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] /= magnitude;
        }

        log.debug("Generated mock embedding for text of length {}", text.length());
        return vector;
    }

    public List<float[]> embedBatch(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }
}