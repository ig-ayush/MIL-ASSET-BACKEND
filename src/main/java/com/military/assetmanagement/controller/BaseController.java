package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.base.BaseRequest;
import com.military.assetmanagement.dto.base.BaseResponse;
import com.military.assetmanagement.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bases")
@RequiredArgsConstructor
public class BaseController {

    private final BaseService baseService;

    @GetMapping
    public ResponseEntity<List<BaseResponse>> getAllBases() {
        return ResponseEntity.ok(baseService.getAllBases());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getBaseById(@PathVariable Long id) {
        return ResponseEntity.ok(baseService.getBaseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> createBase(@Valid @RequestBody BaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(baseService.createBase(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> updateBase(@PathVariable Long id, @Valid @RequestBody BaseRequest request) {
        return ResponseEntity.ok(baseService.updateBase(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBase(@PathVariable Long id) {
        baseService.deleteBase(id);
        return ResponseEntity.noContent().build();
    }
}
