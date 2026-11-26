package com.electrostock.catalog.dto;

import java.time.Instant;
import java.util.Map;

public record ComponentResponse(
    String id,
    String partNumber,
    String name,
    String categoryId,
    String categoryCode,
    String categoryName,
    boolean categoryActive,
    String manufacturer,
    String packageType,
    String unit,
    String storageLocation,
    int reorderThreshold,
    Map<String, String> specifications,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
