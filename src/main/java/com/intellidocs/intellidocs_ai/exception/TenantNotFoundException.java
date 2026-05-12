package com.intellidocs.intellidocs_ai.exception;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String id) {
        super("Tenant with id " + id + " not found");
    }
}
