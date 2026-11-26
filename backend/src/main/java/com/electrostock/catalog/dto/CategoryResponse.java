package com.electrostock.catalog.dto;

import java.time.Instant;

public record CategoryResponse(
    String id,
    String code,
    String name,
    String description,
    boolean active,
    long componentCount,
    long activeComponentCount,
    Instant createdAt,
    Instant updatedAt
) {
}
