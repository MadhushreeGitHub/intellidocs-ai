package com.intellidocs.intellidocs_ai.service.document;

import groovy.util.logging.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@lombok.extern.slf4j.Slf4j
@Slf4j
@Service
public class TikaService {

    // Tika is thread-safe — one instance is enough for the whole app
    private final Tika tika = new Tika();

    public String extractingText(String filePath){
        try{
            Path path = Paths.get(filePath);

            if(!Files.exists(path)){
                throw new RuntimeException("File not found: " + filePath);

            }

            // Tika auto-detects file type and uses correct parser
            // Works for PDF, DOCX, TXT, HTML, Excel, PowerPoint...

            String text = tika.parseToString(path.toFile());
            log.info("Successfully extracted text from {} ({} characters)", path.getFileName(), text.length());
            return text;
        }catch (IOException | TikaException e){
            log.error("Text extraction failed for {} : {}",filePath, e.getMessage());
            throw new RuntimeException("Failed to extract text from file: " + e.getMessage(), e);
        }
    }
}
