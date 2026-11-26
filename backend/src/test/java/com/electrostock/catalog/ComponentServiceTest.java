package com.electrostock.catalog;

import com.electrostock.catalog.dto.ComponentRequest;
import com.electrostock.catalog.dto.ComponentResponse;
import com.electrostock.catalog.exception.BusinessRuleException;
import com.electrostock.catalog.exception.DuplicateResourceException;
import com.electrostock.catalog.model.Category;
import com.electrostock.catalog.model.Component;
import com.electrostock.catalog.repository.CategoryRepository;
import com.electrostock.catalog.repository.ComponentRepository;
import com.electrostock.catalog.service.ComponentService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
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
class ComponentServiceTest {

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private ComponentService componentService;

    @BeforeEach
    void setUp() {
        componentService = new ComponentService(componentRepository, categoryRepository, mongoTemplate);
    }

    @Test
    void createSavesComponentWithSpecificationsUnitLocationAndThreshold() {
        Category category = category("cat-1", "RES", "Resistors", true);
        when(componentRepository.existsByPartNumberNormalized("RES-10K-0603")).thenReturn(false);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(componentRepository.save(any(Component.class))).thenAnswer(invocation -> {
            Component saved = invocation.getArgument(0);
            saved.setId("cmp-1");
            return saved;
        });

        Map<String, String> rawSpecs = new LinkedHashMap<>();
        rawSpecs.put(" Resistance ", " 10 kOhm ");
        rawSpecs.put("Tolerance", "±1%");

        ComponentResponse response = componentService.create(
            new ComponentRequest(
                "RES-10K-0603",
                "10k SMD Resistor",
                "cat-1",
                "Yageo",
                "0603",
                " pcs ",
                " Rack A · Bin 04 ",
                50,
                rawSpecs,
                "1% tolerance"
            )
        );

        assertThat(response.id()).isEqualTo("cmp-1");
        assertThat(response.partNumber()).isEqualTo("RES-10K-0603");
        assertThat(response.categoryCode()).isEqualTo("RES");
        assertThat(response.categoryName()).isEqualTo("Resistors");
        assertThat(response.unit()).isEqualTo("pcs");
        assertThat(response.storageLocation()).isEqualTo("Rack A · Bin 04");
        assertThat(response.reorderThreshold()).isEqualTo(50);
        assertThat(response.specifications())
            .containsEntry("Resistance", "10 kOhm")
            .containsEntry("Tolerance", "±1%");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createRejectsDuplicatePartNumber() {
        when(componentRepository.existsByPartNumberNormalized("RES-10K-0603")).thenReturn(true);

        assertThatThrownBy(() -> componentService.create(
            new ComponentRequest("res-10k-0603", "10k SMD Resistor", "cat-1", null, null, "pcs", "Bin A1", 10, null, null)
        ))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("res-10k-0603");
    }

    @Test
    void createRejectsDuplicateSpecificationKeysIgnoringCase() {
        Category category = category("cat-1", "RES", "Resistors", true);
        when(componentRepository.existsByPartNumberNormalized("RES-10K-0603")).thenReturn(false);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        Map<String, String> specs = new LinkedHashMap<>();
        specs.put("Voltage", "50V");
        specs.put("voltage", "25V");

        assertThatThrownBy(() -> componentService.create(
            new ComponentRequest("RES-10K-0603", "10k SMD Resistor", "cat-1", null, null, "pcs", "Bin A1", 10, specs, null)
        ))
            .isInstanceOf(BusinessRuleException.class)
            .satisfies(ex -> assertThat(((BusinessRuleException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST))
            .hasMessageContaining("Duplicate");
    }

    @Test
    void createRejectsInactiveCategory() {
        Category inactiveCategory = category("cat-1", "RES", "Resistors", false);
        when(componentRepository.existsByPartNumberNormalized("RES-10K-0603")).thenReturn(false);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(inactiveCategory));

        assertThatThrownBy(() -> componentService.create(
            new ComponentRequest("RES-10K-0603", "10k SMD Resistor", "cat-1", null, null, "pcs", "Bin A1", 10, null, null)
        ))
            .isInstanceOf(BusinessRuleException.class)
            .satisfies(ex -> assertThat(((BusinessRuleException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void activateRejectsIfCategoryIsInactive() {
        Component inactiveComponent = new Component();
        inactiveComponent.setId("cmp-1");
        inactiveComponent.setPartNumber("RES-10K-0603");
        inactiveComponent.setPartNumberNormalized("RES-10K-0603");
        inactiveComponent.setName("10k SMD Resistor");
        inactiveComponent.setCategoryId("cat-1");
        inactiveComponent.setUnit("pcs");
        inactiveComponent.setStorageLocation("Bin A1");
        inactiveComponent.setReorderThreshold(10);
        inactiveComponent.setActive(false);

        Category inactiveCategory = category("cat-1", "RES", "Resistors", false);
        when(componentRepository.findById("cmp-1")).thenReturn(Optional.of(inactiveComponent));
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(inactiveCategory));

        assertThatThrownBy(() -> componentService.activate("cmp-1"))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("inactive");
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
