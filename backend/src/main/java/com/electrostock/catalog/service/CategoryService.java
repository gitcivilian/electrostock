package com.electrostock.catalog.service;

import com.electrostock.catalog.dto.CategoryRequest;
import com.electrostock.catalog.dto.CategoryResponse;
import com.electrostock.catalog.exception.BusinessRuleException;
import com.electrostock.catalog.exception.DuplicateResourceException;
import com.electrostock.catalog.exception.ResourceNotFoundException;
import com.electrostock.catalog.model.Category;
import com.electrostock.catalog.model.RecordStatus;
import com.electrostock.catalog.repository.CategoryRepository;
import com.electrostock.catalog.repository.ComponentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ComponentRepository componentRepository;
    private final MongoTemplate mongoTemplate;

    public CategoryService(
        CategoryRepository categoryRepository,
        ComponentRepository componentRepository,
        MongoTemplate mongoTemplate
    ) {
        this.categoryRepository = categoryRepository;
        this.componentRepository = componentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<CategoryResponse> list(String search, String statusParam) {
        RecordStatus status = RecordStatus.fromParam(statusParam);
        Query query = CatalogQueryHelper.buildFilterQuery(
            search,
            status,
            null,
            Sort.by(Sort.Direction.ASC, "name"),
            "code",
            "name",
            "description"
        );
        return mongoTemplate.find(query, Category.class).stream()
            .map(this::toResponse)
            .toList();
    }

    public CategoryResponse getById(String id) {
        return toResponse(findCategoryOrThrow(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        String code = normalizeCode(request.code());
        String name = request.name().trim();
        String nameNormalized = normalizeName(name);

        if (categoryRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Category code '" + code + "' already exists.");
        }
        if (categoryRepository.existsByNameNormalized(nameNormalized)) {
            throw new DuplicateResourceException("Category name '" + name + "' already exists.");
        }

        Instant now = Instant.now();
        Category category = new Category();
        category.setCode(code);
        category.setName(name);
        category.setNameNormalized(nameNormalized);
        category.setDescription(CatalogQueryHelper.trimToNull(request.description()));
        category.setActive(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(String id, CategoryRequest request) {
        Category category = findCategoryOrThrow(id);
        String code = normalizeCode(request.code());
        String name = request.name().trim();
        String nameNormalized = normalizeName(name);

        if (categoryRepository.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("Category code '" + code + "' already exists.");
        }
        if (categoryRepository.existsByNameNormalizedAndIdNot(nameNormalized, id)) {
            throw new DuplicateResourceException("Category name '" + name + "' already exists.");
        }

        category.setCode(code);
        category.setName(name);
        category.setNameNormalized(nameNormalized);
        category.setDescription(CatalogQueryHelper.trimToNull(request.description()));
        category.setUpdatedAt(Instant.now());

        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse deactivate(String id) {
        Category category = findCategoryOrThrow(id);
        if (!category.isActive()) {
            return toResponse(category);
        }
        long activeComponents = componentRepository.countByCategoryIdAndActiveTrue(id);
        if (activeComponents > 0) {
            throw new BusinessRuleException(
                HttpStatus.CONFLICT,
                "Cannot deactivate category '" + category.getName()
                    + "' because it has " + activeComponents + " active component(s). Deactivate or reassign them first."
            );
        }
        category.setActive(false);
        category.setUpdatedAt(Instant.now());
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse activate(String id) {
        Category category = findCategoryOrThrow(id);
        if (category.isActive()) {
            return toResponse(category);
        }
        category.setActive(true);
        category.setUpdatedAt(Instant.now());
        return toResponse(categoryRepository.save(category));
    }

    public Category findCategoryOrThrow(String id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        long totalCount = componentRepository.countByCategoryId(category.getId());
        long activeCount = componentRepository.countByCategoryIdAndActiveTrue(category.getId());
        return new CategoryResponse(
            category.getId(),
            category.getCode(),
            category.getName(),
            category.getDescription(),
            category.isActive(),
            totalCount,
            activeCount,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
