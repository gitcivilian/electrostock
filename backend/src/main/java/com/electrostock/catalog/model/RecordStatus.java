package com.electrostock.catalog.model;

import com.electrostock.catalog.exception.BusinessRuleException;
import java.util.Locale;
import org.springframework.http.HttpStatus;

public enum RecordStatus {
    ALL,
    ACTIVE,
    INACTIVE;

    public static RecordStatus fromParam(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return RecordStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException(
                HttpStatus.BAD_REQUEST,
                "Invalid status filter '" + value + "'. Allowed values: ALL, ACTIVE, INACTIVE."
            );
        }
    }
}
