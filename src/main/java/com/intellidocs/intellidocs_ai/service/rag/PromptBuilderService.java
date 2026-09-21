package com.intellidocs.intellidocs_ai.service.rag;


// Assembles the retrieved chunks + the user's question into a single
// prompt string for the LLM. This is the "Augmented" step in RAG:
// we augment the model's prompt with our own document context.

import com.intellidocs.intellidocs_ai.service.search.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PromptBuilderService {
    // The instruction block. It does two critical jobs:
    //  1. Tells the LLM to answer USING the context (not its own training)
    //  2. Tells it to say "I don't know" if the answer isn't in the context
    //     — this is the main defense against hallucination.
    private static final String SYSTEM_INSTRUCTION= """
            You are a helpful assistant for the IntelliDocs system.
            Answer the user's question using ONLY the context provided below.
            If the answer is not in the context, say "I don't know based on the available documents."
            Do not make up information.
            """;

    public String buildPrompt(String question, List<SearchService.ScoredChunk> retrievedChunks) {
        // If retrieval found nothing (all below threshold), there's no context.
        // We still build a prompt, but the LLM will correctly say "I don't know".
        if(retrievedChunks == null || retrievedChunks.size() == 0){
            log.info("No chunks passed to prompt builder — context will be empty for question {}", question);
        }
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_INSTRUCTION).append("\n");
        promptBuilder.append("Context:\n");

        // Number each chunk so the context is readable and traceable.
        int chunkNumber = 1;
        for (SearchService.ScoredChunk scoredChunk : retrievedChunks) {
            promptBuilder.append("[").append(chunkNumber).append("] ")
                    .append(scoredChunk.chunk().getContent().trim())
                    .append("\n\n");
            chunkNumber++;
        }

        promptBuilder.append("Question: ").append(question).append("\n");
        promptBuilder.append("Answer: ");

        String finalPrompt = promptBuilder.toString();
        log.info("Built prompt: {} chars, {} context chunks ",
                finalPrompt.length(), retrievedChunks.size());
        return finalPrompt;


    }

}
