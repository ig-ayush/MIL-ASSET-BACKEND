package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.dashboard.DashboardResponse;
import com.military.assetmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/v1/dashboard?baseId=1
     * ADMIN: baseId optional (null = aggregate all)
     * BASE_COMMANDER: baseId ignored; always returns their own base
     * LOGISTICS_OFFICER: baseId must match or null
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) Long baseId) {
        return ResponseEntity.ok(dashboardService.getDashboard(baseId));
    }
}
