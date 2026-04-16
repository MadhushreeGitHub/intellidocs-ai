package com.intellidocs.intellidocs_ai.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private  final String message;
    private  final T data;
    private  final Object errors;

    @Builder.Default
    private  final Instant timestamp = Instant.now();

    //Static factory methods - cleaner call sites than the builder
    public static <T> ApiResponse<T> ok(T data){
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data){
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message){
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }


}
