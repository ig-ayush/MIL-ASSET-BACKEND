package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.expenditure.ExpenditureRequest;
import com.military.assetmanagement.dto.expenditure.ExpenditureResponse;
import com.military.assetmanagement.service.ExpenditureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenditures")
@RequiredArgsConstructor
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @GetMapping
    public ResponseEntity<List<ExpenditureResponse>> getAllExpenditures(
            @RequestParam(required = false) Long baseId) {
        return ResponseEntity.ok(expenditureService.getAll(baseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenditureResponse> getExpenditureById(@PathVariable Long id) {
        return ResponseEntity.ok(expenditureService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ExpenditureResponse> createExpenditure(@Valid @RequestBody ExpenditureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenditureService.create(request));
    }
}
