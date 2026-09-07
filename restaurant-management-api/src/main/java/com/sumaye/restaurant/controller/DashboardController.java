package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.OwnerDashboardResponse;
import com.sumaye.restaurant.service.DashboardReportService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branches/{branchId}/dashboard")
public class DashboardController {

    private final DashboardReportService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public OwnerDashboardResponse dashboard(
            @PathVariable Long branchId,
            Authentication authentication,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false, defaultValue = "DAY") String groupBy) {
        return service.dashboard(branchId, authentication.getName(), from, to, groupBy);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MANAGER')")
    public byte[] export(
            @PathVariable Long branchId,
            Authentication authentication,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false, defaultValue = "DAY") String groupBy,
            @RequestParam(defaultValue = "pdf") String format,
            HttpServletResponse response) {
        boolean pdf = !"csv".equalsIgnoreCase(format);
        byte[] bytes = pdf
                ? service.exportPdf(branchId, authentication.getName(), from, to, groupBy)
                : service.exportCsv(branchId, authentication.getName(), from, to, groupBy);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        response.setContentType(pdf ? "application/pdf" : "text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=riporti-biashara-" + timestamp + (pdf ? ".pdf" : ".csv"));
        return bytes;
    }
}