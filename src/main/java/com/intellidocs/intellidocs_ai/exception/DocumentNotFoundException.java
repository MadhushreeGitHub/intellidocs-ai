package com.intellidocs.intellidocs_ai.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String id) {
        super("Document with id " + id + " not found");
    }
}
