package com.intellidocs.intellidocs_ai.exception;

public class DuplicateDocumentException extends RuntimeException{
    public DuplicateDocumentException() {
        super("This Document already exists");
    }
}
