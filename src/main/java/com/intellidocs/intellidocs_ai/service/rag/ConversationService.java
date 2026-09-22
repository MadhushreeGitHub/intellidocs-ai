package com.intellidocs.intellidocs_ai.service.rag;

import com.intellidocs.intellidocs_ai.domain.entity.Conversation;
import com.intellidocs.intellidocs_ai.domain.entity.Message;
import com.intellidocs.intellidocs_ai.domain.entity.MessageSource;
import com.intellidocs.intellidocs_ai.repository.ConversationRepository;
import com.intellidocs.intellidocs_ai.repository.MessageRepository;
import com.intellidocs.intellidocs_ai.repository.MessageSourceRepository;
import com.intellidocs.intellidocs_ai.service.search.SearchService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {
    private final MessageSourceRepository messageSourceRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Value("${app.rag.history-turns:3}")
    private String historyTurns; //how many recent messages to include

    //Load the most recent messages for a conversation, oldest first for the prompt
    public List<Message> getRecentHistory(UUID conversationId) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, Integer.parseInt(historyTurns) * 2); // *2: user+assistant per turn
        List<Message> recent = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        java.util.Collections.reverse(recent); //back to chronological order
        return recent;
    }

    // Saves one full Q&A turn: the user question, the assistant answer,
    // and one message_sources row per chunk the answer cited.
    // @Transactional: all writes succeed together or none do
    @Transactional
    public UUID saveTurn(UUID tenantId, UUID userId,UUID conversationId,
                         String question, String answer, List<SearchService.ScoredChunk> citedChunks) {

        // 1. Create the conversation (Day 13: one turn = one new conversation;
        //    Day 14 will reuse an existing conversation for follow-ups)
        UUID convId = conversationId;
        if (convId == null) {
            Conversation conversation = conversationRepository.save(
                    Conversation.builder()
                            .tenantId(tenantId).userId(userId)
                            .title(truncateTitle(question)).build());
            convId = conversation.getId();
        }

        // 2. Save the user's question
        messageRepository.save(Message.builder()
                .conversationId(convId)
                .tenantId(tenantId)
                .role("user")
                .content(question)
                .build());

        //3. Save the assistant's answer
        Message assistantMessage = messageRepository.save(Message.builder()
                .conversationId(convId)
                .tenantId(tenantId)
                .role("assistant")
                .content(answer)
                .build());

        //4. Save one scource row per cited chunk (the citation trail)
        for (SearchService.ScoredChunk scoredChunk : citedChunks) {
            messageSourceRepository.save(MessageSource.builder()
                            .messageId(assistantMessage.getId())
                            .chunkId(scoredChunk.chunk().getId())
                            .score(scoredChunk.score())
                            .build());
        }

        log.info("Saved conversation {} with {} cited sources",
                convId, citedChunks.size());

        return convId;

    }

    private String truncateTitle(String question) {
        return question.length() >= 500?
                question.substring(0, 500):
                question;
    }

}
