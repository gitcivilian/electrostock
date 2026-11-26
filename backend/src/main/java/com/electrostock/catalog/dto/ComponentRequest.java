package com.electrostock.catalog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record ComponentRequest(
    @NotBlank(message = "Part number is required")
    @Size(max = 48, message = "Part number must be 48 characters or fewer")
    @Pattern(
        regexp = "^[A-Za-z0-9][A-Za-z0-9._/-]*$",
        message = "Part number may contain only letters, numbers, dots, slashes, hyphens, and underscores"
    )
    String partNumber,

    @NotBlank(message = "Component name is required")
    @Size(max = 120, message = "Component name must be 120 characters or fewer")
    String name,

    @NotBlank(message = "Category is required")
    String categoryId,

    @Size(max = 80, message = "Manufacturer must be 80 characters or fewer")
    String manufacturer,

    @Size(max = 40, message = "Package type must be 40 characters or fewer")
    String packageType,

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 24, message = "Unit of measure must be 24 characters or fewer")
    String unit,

    @NotBlank(message = "Storage location is required")
    @Size(max = 80, message = "Storage location must be 80 characters or fewer")
    String storageLocation,

    @NotNull(message = "Reorder threshold is required")
    @Min(value = 0, message = "Reorder threshold cannot be negative")
    @Max(value = 1000000, message = "Reorder threshold must be 1,000,000 or less")
    Integer reorderThreshold,

    Map<String, String> specifications,

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    String description
) {
}
