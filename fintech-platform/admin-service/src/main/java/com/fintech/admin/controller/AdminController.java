package com.fintech.admin.controller;

import com.fintech.admin.dto.DashboardMetricsDTO;
import com.fintech.admin.dto.TransactionReportDTO;
import com.fintech.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Administrative dashboard endpoints for reports and metrics")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/metrics")
    @Operation(summary = "Get dashboard metrics", description = "Returns aggregated metrics for the admin dashboard")
    public ResponseEntity<DashboardMetricsDTO> getDashboardMetrics() {
        return ResponseEntity.ok(adminService.getDashboardMetrics());
    }

    @GetMapping("/reports/transactions")
    @Operation(summary = "Get transaction report", description = "Returns transaction report for a date range")
    public ResponseEntity<List<TransactionReportDTO>> getTransactionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(adminService.getTransactionReport(startDate, endDate));
    }

    @PostMapping("/cache/refresh")
    @Operation(summary = "Refresh cache", description = "Invalidates cached data to force refresh on next request")
    public ResponseEntity<Void> refreshCache() {
        adminService.refreshCache();
        return ResponseEntity.noContent().build();
    }
}
