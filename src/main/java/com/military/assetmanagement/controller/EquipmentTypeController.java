package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.equipment.EquipmentTypeRequest;
import com.military.assetmanagement.dto.equipment.EquipmentTypeResponse;
import com.military.assetmanagement.service.EquipmentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment-types")
@RequiredArgsConstructor
public class EquipmentTypeController {

    private final EquipmentTypeService equipmentTypeService;

    @GetMapping
    public ResponseEntity<List<EquipmentTypeResponse>> getAllEquipmentTypes() {
        return ResponseEntity.ok(equipmentTypeService.getAllEquipmentTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentTypeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentTypeResponse> create(@Valid @RequestBody EquipmentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentTypeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentTypeResponse> update(@PathVariable Long id, @Valid @RequestBody EquipmentTypeRequest request) {
        return ResponseEntity.ok(equipmentTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
