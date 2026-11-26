package com.electrostock.catalog;

import com.electrostock.catalog.dto.CategoryRequest;
import com.electrostock.catalog.dto.CategoryResponse;
import com.electrostock.catalog.exception.BusinessRuleException;
import com.electrostock.catalog.exception.DuplicateResourceException;
import com.electrostock.catalog.model.Category;
import com.electrostock.catalog.repository.CategoryRepository;
import com.electrostock.catalog.repository.ComponentRepository;
import com.electrostock.catalog.service.CategoryService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, componentRepository, mongoTemplate);
    }

    @Test
    void createNormalizesCodeAndSetsActiveTrue() {
        when(categoryRepository.existsByCode("RES")).thenReturn(false);
        when(categoryRepository.existsByNameNormalized("resistors")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId("cat-1");
            return saved;
        });
        when(componentRepository.countByCategoryId("cat-1")).thenReturn(0L);
        when(componentRepository.countByCategoryIdAndActiveTrue("cat-1")).thenReturn(0L);

        CategoryResponse response = categoryService.create(
            new CategoryRequest(" res ", " Resistors ", " Fixed and variable resistors ")
        );

        assertThat(response.id()).isEqualTo("cat-1");
        assertThat(response.code()).isEqualTo("RES");
        assertThat(response.name()).isEqualTo("Resistors");
        assertThat(response.description()).isEqualTo("Fixed and variable resistors");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createRejectsDuplicateCode() {
        when(categoryRepository.existsByCode("RES")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("res", "Resistors", null)))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("RES");
    }

    @Test
    void deactivateRejectsWhenActiveComponentsExist() {
        Category existing = category("cat-1", "RES", "Resistors", true);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(existing));
        when(componentRepository.countByCategoryIdAndActiveTrue("cat-1")).thenReturn(2L);

        assertThatThrownBy(() -> categoryService.deactivate("cat-1"))
            .isInstanceOf(BusinessRuleException.class)
            .satisfies(ex -> assertThat(((BusinessRuleException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT))
            .hasMessageContaining("2 active component(s)");
    }

    @Test
    void deactivateAndActivateUpdateStatus() {
        Category existing = category("cat-1", "RES", "Resistors", true);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(existing));
        when(componentRepository.countByCategoryIdAndActiveTrue("cat-1")).thenReturn(0L);
        when(componentRepository.countByCategoryId("cat-1")).thenReturn(0L);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse deactivated = categoryService.deactivate("cat-1");
        assertThat(deactivated.active()).isFalse();

        CategoryResponse reactivated = categoryService.activate("cat-1");
        assertThat(reactivated.active()).isTrue();
    }

    private static Category category(String id, String code, String name, boolean active) {
        Category category = new Category();
        category.setId(id);
        category.setCode(code);
        category.setName(name);
        category.setNameNormalized(name.toLowerCase());
        category.setActive(active);
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());
        return category;
    }
}
