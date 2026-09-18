package com.intellidocs.intellidocs_ai.service.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Splits text into chunks on paragraph boundaries (blank lines),
// so related text stays together. Falls back to a sliding window
// only if a single paragraph is bigger than the size limit.
@Slf4j
@Service
public class ChunkingService {

    @Value("${app.rag.chunk-size:200}")
    private int chunkSize;      // max words per chunk

    @Value("${app.rag.chunk-overlap:30}")
    private int chunkOverlap;   // overlap, used only in the sliding-window fallback

    public record Chunk(String content, int index, int tokenCount) {}

    public List<Chunk> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // STEP 1: break the text into paragraphs at blank lines.
        // "\n\s*\n" means: a newline, optional spaces, another newline.
        String[] paragraphs = text.split("\\n\\s*\\n");

        List<Chunk> chunks = new ArrayList<>();
        int index = 0;

        // "buffer" holds paragraphs we are packing into the current chunk
        StringBuilder buffer = new StringBuilder();
        int bufferWords = 0;

        // STEP 2: walk through each paragraph and pack them into chunks
        for (String para : paragraphs) {
            para = para.trim();
            if (para.isBlank()) continue;

            int paraWords = para.split("\\s+").length;

            // CASE A: this single paragraph is too big on its own.
            // Flush whatever is buffered, then sliding-window the big paragraph.
            if (paraWords > chunkSize) {
                if (bufferWords > 0) {
                    chunks.add(new Chunk(buffer.toString().trim(), index++, bufferWords));
                    buffer.setLength(0);
                    bufferWords = 0;
                }
                for (Chunk c : slidingWindow(para, index)) {
                    chunks.add(c);
                    index++;
                }
                continue;
            }

            // CASE B: adding this paragraph would overflow the chunk.
            // Close the current chunk first, then start a new one with this paragraph.
            if (bufferWords + paraWords > chunkSize && bufferWords > 0) {
                chunks.add(new Chunk(buffer.toString().trim(), index++, bufferWords));
                buffer.setLength(0);
                bufferWords = 0;
            }

            // CASE C: normal case — add the paragraph to the current chunk.
            buffer.append(para).append("\n\n");
            bufferWords += paraWords;
        }

        // STEP 3: flush any leftover paragraphs into a final chunk
        if (bufferWords > 0) {
            chunks.add(new Chunk(buffer.toString().trim(), index++, bufferWords));
        }

        log.info("Boundary-aware split into {} chunks ({} paragraphs)",
                chunks.size(), paragraphs.length);
        return chunks;
    }

    // Fallback: old sliding-window logic, used only for a paragraph
    // that is larger than chunkSize on its own. Takes a starting index
    // so chunk numbering stays continuous.
    private List<Chunk> slidingWindow(String text, int startIndex) {
        String[] words = text.split("\\s+");
        List<Chunk> chunks = new ArrayList<>();
        int start = 0;
        int index = startIndex;

        while (start < words.length) {
            int end = Math.min(start + chunkSize, words.length);
            String chunkText = String.join(" ", Arrays.copyOfRange(words, start, end));
            chunks.add(new Chunk(chunkText, index++, end - start));

            start += (chunkSize - chunkOverlap);
            if (chunkSize <= chunkOverlap) break;  // safety: avoid infinite loop
        }
        return chunks;
    }
}