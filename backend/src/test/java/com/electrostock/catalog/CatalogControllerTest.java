package com.electrostock.catalog;

import com.electrostock.catalog.controller.CategoryController;
import com.electrostock.catalog.controller.ComponentController;
import com.electrostock.catalog.dto.CategoryResponse;
import com.electrostock.catalog.dto.ComponentResponse;
import com.electrostock.catalog.exception.CatalogExceptionHandler;
import com.electrostock.catalog.exception.DuplicateResourceException;
import com.electrostock.catalog.service.CategoryService;
import com.electrostock.catalog.service.ComponentService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CategoryController.class, ComponentController.class})
@Import(CatalogExceptionHandler.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private ComponentService componentService;

    @Test
    void listsAndFiltersCategories() throws Exception {
        CategoryResponse res = new CategoryResponse(
            "cat-1", "RES", "Resistors", "Fixed resistors", true, 3, 2, Instant.now(), Instant.now()
        );
        when(categoryService.list("res", "ACTIVE")).thenReturn(List.of(res));

        mvc.perform(get("/api/categories").param("search", "res").param("status", "ACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("RES"))
            .andExpect(jsonPath("$[0].activeComponentCount").value(2));
    }

    @Test
    void rejectsInvalidCategoryRequestWithFieldErrors() throws Exception {
        mvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"invalid code!\",\"name\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void rejectsNegativeThresholdAndMissingLocationOrUnit() throws Exception {
        mvc.perform(post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"partNumber\":\"RES-10K\",\"name\":\"10k Resistor\",\"categoryId\":\"cat-1\",\"unit\":\"\",\"storageLocation\":\"\",\"reorderThreshold\":-5}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createsUpdatesAndDeactivatesComponent() throws Exception {
        ComponentResponse created = new ComponentResponse(
            "cmp-1", "MCU-ESP32-WROOM", "ESP32-WROOM-32", "cat-2", "MCU", "Microcontrollers", true,
            "Espressif", "SMD-38", "pcs", "Cabinet B · Tray 2", 15,
            Map.of("Voltage", "3.3V", "Flash", "4MB"),
            "Wi-Fi & BLE MCU", true, Instant.now(), Instant.now()
        );
        ComponentResponse updated = new ComponentResponse(
            "cmp-1", "MCU-ESP32-WROOM", "ESP32-WROOM-32E", "cat-2", "MCU", "Microcontrollers", true,
            "Espressif", "SMD-38", "trays", "Cabinet B · Tray 3", 20,
            Map.of("Voltage", "3.3V", "Flash", "8MB"),
            "Updated revision", true, Instant.now(), Instant.now()
        );
        ComponentResponse deactivated = new ComponentResponse(
            "cmp-1", "MCU-ESP32-WROOM", "ESP32-WROOM-32E", "cat-2", "MCU", "Microcontrollers", true,
            "Espressif", "SMD-38", "trays", "Cabinet B · Tray 3", 20,
            Map.of("Voltage", "3.3V", "Flash", "8MB"),
            "Updated revision", false, Instant.now(), Instant.now()
        );

        when(componentService.create(any())).thenReturn(created);
        when(componentService.update(eq("cmp-1"), any())).thenReturn(updated);
        when(componentService.deactivate("cmp-1")).thenReturn(deactivated);

        mvc.perform(post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "partNumber": "MCU-ESP32-WROOM",
                      "name": "ESP32-WROOM-32",
                      "categoryId": "cat-2",
                      "unit": "pcs",
                      "storageLocation": "Cabinet B · Tray 2",
                      "reorderThreshold": 15,
                      "specifications": {"Voltage": "3.3V", "Flash": "4MB"}
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.partNumber").value("MCU-ESP32-WROOM"))
            .andExpect(jsonPath("$.unit").value("pcs"))
            .andExpect(jsonPath("$.storageLocation").value("Cabinet B · Tray 2"))
            .andExpect(jsonPath("$.reorderThreshold").value(15))
            .andExpect(jsonPath("$.specifications.Voltage").value("3.3V"));

        mvc.perform(put("/api/components/cmp-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "partNumber": "MCU-ESP32-WROOM",
                      "name": "ESP32-WROOM-32E",
                      "categoryId": "cat-2",
                      "unit": "trays",
                      "storageLocation": "Cabinet B · Tray 3",
                      "reorderThreshold": 20,
                      "specifications": {"Voltage": "3.3V", "Flash": "8MB"}
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("ESP32-WROOM-32E"))
            .andExpect(jsonPath("$.reorderThreshold").value(20));

        mvc.perform(patch("/api/components/cmp-1/deactivate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void returnsConflictOnDuplicatePartNumber() throws Exception {
        when(componentService.create(any()))
            .thenThrow(new DuplicateResourceException("Part number 'RES-10K' already exists."));

        mvc.perform(post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "partNumber": "RES-10K",
                      "name": "10k Resistor",
                      "categoryId": "cat-1",
                      "unit": "pcs",
                      "storageLocation": "Bin A1",
                      "reorderThreshold": 25
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Part number 'RES-10K' already exists."));
    }
}
