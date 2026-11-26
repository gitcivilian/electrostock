package com.electrostock.catalog.controller;

import com.electrostock.catalog.dto.ComponentRequest;
import com.electrostock.catalog.dto.ComponentResponse;
import com.electrostock.catalog.service.ComponentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private final ComponentService componentService;

    public ComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    @GetMapping
    public List<ComponentResponse> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String categoryId,
        @RequestParam(required = false, defaultValue = "ALL") String status
    ) {
        return componentService.list(search, categoryId, status);
    }

    @GetMapping("/{id}")
    public ComponentResponse getById(@PathVariable String id) {
        return componentService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComponentResponse create(@Valid @RequestBody ComponentRequest request) {
        return componentService.create(request);
    }

    @PutMapping("/{id}")
    public ComponentResponse update(@PathVariable String id, @Valid @RequestBody ComponentRequest request) {
        return componentService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public ComponentResponse deactivate(@PathVariable String id) {
        return componentService.deactivate(id);
    }

    @PatchMapping("/{id}/activate")
    public ComponentResponse activate(@PathVariable String id) {
        return componentService.activate(id);
    }
}
