package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.inventory.InventoryResponse;
import com.military.assetmanagement.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getInventory(
            @RequestParam(required = false) Long baseId) {
        return ResponseEntity.ok(inventoryService.getInventoryForBase(baseId));
    }
}
