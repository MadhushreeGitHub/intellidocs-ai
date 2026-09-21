package com.intellidocs.intellidocs_ai.service.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/***
 * Calls the Ollama chat model (gemma3) to generate an answer
 * from the assembled RAG prompt. this is the "Generation" Step.
 */

@Slf4j
@Service
public class ChatService {

    private final ChatModel chatModel;

    ///Spring AI creates an "OllamaChatModel" bean when chat is enabled.
    /// We qualify it explicity, like we did for the embadding model.

    public ChatService(@Qualifier("ollamaChatModel") ChatModel chatModel){
        this.chatModel = chatModel;
    }

    public String generateAnswer(String propmt){
        log.info("Generating answer for prompt ({} chars)", propmt.length());
        try {
            String answer = chatModel.call(propmt); // sends prompt, returns generated text
            log.info("Generated Answer: ({} chars)", answer.length());
            return answer;
        }catch (Exception e){
            log.error("Generation failed: {}", e.getMessage());
            throw new RuntimeException("Answer generation failed " + e);
        }

    }



}
