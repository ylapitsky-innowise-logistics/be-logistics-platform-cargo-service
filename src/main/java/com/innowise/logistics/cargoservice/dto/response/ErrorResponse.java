package com.innowise.logistics.cargoservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
//@NoArgsConstructor
//@AllArgsConstructor
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
//    private Map<String, String> details; // для детальных ошибок валидации
}

