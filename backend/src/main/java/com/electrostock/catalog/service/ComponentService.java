package com.electrostock.catalog.service;

import com.electrostock.catalog.dto.ComponentRequest;
import com.electrostock.catalog.dto.ComponentResponse;
import com.electrostock.catalog.exception.BusinessRuleException;
import com.electrostock.catalog.exception.DuplicateResourceException;
import com.electrostock.catalog.exception.ResourceNotFoundException;
import com.electrostock.catalog.model.Category;
import com.electrostock.catalog.model.Component;
import com.electrostock.catalog.model.RecordStatus;
import com.electrostock.catalog.repository.CategoryRepository;
import com.electrostock.catalog.repository.ComponentRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ComponentService {

    private static final int MAX_SPECIFICATIONS = 25;
    private static final int MAX_SPEC_KEY_LENGTH = 40;
    private static final int MAX_SPEC_VALUE_LENGTH = 120;

    private final ComponentRepository componentRepository;
    private final CategoryRepository categoryRepository;
    private final MongoTemplate mongoTemplate;

    public ComponentService(
        ComponentRepository componentRepository,
        CategoryRepository categoryRepository,
        MongoTemplate mongoTemplate
    ) {
        this.componentRepository = componentRepository;
        this.categoryRepository = categoryRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<ComponentResponse> list(String search, String categoryId, String statusParam) {
        RecordStatus status = RecordStatus.fromParam(statusParam);
        Query query = CatalogQueryHelper.buildFilterQuery(
            search,
            status,
            categoryId,
            Sort.by(Sort.Direction.ASC, "partNumber"),
            "partNumber",
            "name",
            "manufacturer",
            "packageType",
            "unit",
            "storageLocation",
            "specificationsText",
            "description"
        );
        List<Component> components = mongoTemplate.find(query, Component.class);
        if (components.isEmpty()) {
            return List.of();
        }

        Set<String> categoryIds = components.stream()
            .map(Component::getCategoryId)
            .collect(Collectors.toSet());
        Map<String, Category> categoriesById = categoryRepository.findAllById(categoryIds).stream()
            .collect(Collectors.toMap(Category::getId, Function.identity()));

        return components.stream()
            .map(component -> toResponse(component, categoriesById.get(component.getCategoryId())))
            .toList();
    }

    public ComponentResponse getById(String id) {
        Component component = findComponentOrThrow(id);
        Category category = categoryRepository.findById(component.getCategoryId()).orElse(null);
        return toResponse(component, category);
    }

    public ComponentResponse create(ComponentRequest request) {
        String partNumber = request.partNumber().trim();
        String partNumberNormalized = normalizePartNumber(partNumber);
        if (componentRepository.existsByPartNumberNormalized(partNumberNormalized)) {
            throw new DuplicateResourceException("Part number '" + partNumber + "' already exists.");
        }

        Category category = requireActiveCategory(request.categoryId().trim());
        Map<String, String> specifications = normalizeSpecifications(request.specifications());

        Instant now = Instant.now();
        Component component = new Component();
        component.setPartNumber(partNumber);
        component.setPartNumberNormalized(partNumberNormalized);
        applyMutableFields(component, request, category.getId(), specifications, now);
        component.setActive(true);
        component.setCreatedAt(now);

        return toResponse(componentRepository.save(component), category);
    }

    public ComponentResponse update(String id, ComponentRequest request) {
        Component component = findComponentOrThrow(id);
        String partNumber = request.partNumber().trim();
        String partNumberNormalized = normalizePartNumber(partNumber);
        if (componentRepository.existsByPartNumberNormalizedAndIdNot(partNumberNormalized, id)) {
            throw new DuplicateResourceException("Part number '" + partNumber + "' already exists.");
        }

        String requestedCategoryId = request.categoryId().trim();
        Category category;
        if (!requestedCategoryId.equals(component.getCategoryId())) {
            category = requireActiveCategory(requestedCategoryId);
        } else {
            category = findCategoryOrThrow(requestedCategoryId);
        }

        Map<String, String> specifications = normalizeSpecifications(request.specifications());
        component.setPartNumber(partNumber);
        component.setPartNumberNormalized(partNumberNormalized);
        applyMutableFields(component, request, category.getId(), specifications, Instant.now());

        return toResponse(componentRepository.save(component), category);
    }

    public ComponentResponse deactivate(String id) {
        Component component = findComponentOrThrow(id);
        if (component.isActive()) {
            component.setActive(false);
            component.setUpdatedAt(Instant.now());
            component = componentRepository.save(component);
        }
        Category category = categoryRepository.findById(component.getCategoryId()).orElse(null);
        return toResponse(component, category);
    }

    public ComponentResponse activate(String id) {
        Component component = findComponentOrThrow(id);
        Category category = requireActiveCategory(component.getCategoryId());
        if (!component.isActive()) {
            component.setActive(true);
            component.setUpdatedAt(Instant.now());
            component = componentRepository.save(component);
        }
        return toResponse(component, category);
    }

    private Component findComponentOrThrow(String id) {
        return componentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Component not found: " + id));
    }

    private Category findCategoryOrThrow(String categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private Category requireActiveCategory(String categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        if (!category.isActive()) {
            throw new BusinessRuleException(
                HttpStatus.CONFLICT,
                "Category '" + category.getName() + "' is inactive. Activate it or choose an active category."
            );
        }
        return category;
    }

    private static void applyMutableFields(
        Component component,
        ComponentRequest request,
        String categoryId,
        Map<String, String> specifications,
        Instant updatedAt
    ) {
        component.setName(request.name().trim());
        component.setCategoryId(categoryId);
        component.setManufacturer(CatalogQueryHelper.trimToNull(request.manufacturer()));
        component.setPackageType(CatalogQueryHelper.trimToNull(request.packageType()));
        component.setUnit(request.unit().trim());
        component.setStorageLocation(request.storageLocation().trim());
        component.setReorderThreshold(request.reorderThreshold());
        component.setSpecifications(specifications);
        component.setSpecificationsText(formatSpecificationsText(specifications));
        component.setDescription(CatalogQueryHelper.trimToNull(request.description()));
        component.setUpdatedAt(updatedAt);
    }

    private static Map<String, String> normalizeSpecifications(Map<String, String> rawSpecs) {
        if (rawSpecs == null || rawSpecs.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        Set<String> seenKeys = new HashSet<>();

        for (Map.Entry<String, String> entry : rawSpecs.entrySet()) {
            String key = CatalogQueryHelper.trimToNull(entry.getKey());
            String value = CatalogQueryHelper.trimToNull(entry.getValue());
            if (key == null && value == null) {
                continue;
            }
            if (key == null || value == null) {
                throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "Each technical specification must include both a name and a value."
                );
            }
            if (key.length() > MAX_SPEC_KEY_LENGTH) {
                throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "Specification name '" + key + "' must be " + MAX_SPEC_KEY_LENGTH + " characters or fewer."
                );
            }
            if (value.length() > MAX_SPEC_VALUE_LENGTH) {
                throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "Specification value for '" + key + "' must be " + MAX_SPEC_VALUE_LENGTH + " characters or fewer."
                );
            }
            if (!seenKeys.add(key.toLowerCase(Locale.ROOT))) {
                throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate technical specification name: '" + key + "'."
                );
            }
            if (normalized.size() >= MAX_SPECIFICATIONS) {
                throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "At most " + MAX_SPECIFICATIONS + " technical specifications are allowed per component."
                );
            }
            normalized.put(key, value);
        }
        return normalized;
    }

    private static String formatSpecificationsText(Map<String, String> specifications) {
        if (specifications.isEmpty()) {
            return null;
        }
        return specifications.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(" | "));
    }

    private static ComponentResponse toResponse(Component component, Category category) {
        Map<String, String> specs = component.getSpecifications() != null
            ? Collections.unmodifiableMap(new LinkedHashMap<>(component.getSpecifications()))
            : Map.of();
        return new ComponentResponse(
            component.getId(),
            component.getPartNumber(),
            component.getName(),
            component.getCategoryId(),
            category != null ? category.getCode() : null,
            category != null ? category.getName() : "Unknown category",
            category != null && category.isActive(),
            component.getManufacturer(),
            component.getPackageType(),
            component.getUnit(),
            component.getStorageLocation(),
            component.getReorderThreshold(),
            specs,
            component.getDescription(),
            component.isActive(),
            component.getCreatedAt(),
            component.getUpdatedAt()
        );
    }

    private static String normalizePartNumber(String partNumber) {
        return partNumber.trim().toUpperCase(Locale.ROOT);
    }
}
