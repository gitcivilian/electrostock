package com.electrostock.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank(message = "Category code is required")
    @Size(max = 24, message = "Category code must be 24 characters or fewer")
    @Pattern(
        regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$",
        message = "Category code may contain only letters, numbers, hyphens, and underscores"
    )
    String code,

    @NotBlank(message = "Category name is required")
    @Size(max = 80, message = "Category name must be 80 characters or fewer")
    String name,

    @Size(max = 300, message = "Category description must be 300 characters or fewer")
    String description
) {
}
