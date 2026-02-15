package com.example.crudboard.global.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        String code,
        String message,
        List<FieldViolation> fieldViolations,
        String path,
        Instant timestamp
) {
    public record FieldViolation(String field, String message) {

    }
}
