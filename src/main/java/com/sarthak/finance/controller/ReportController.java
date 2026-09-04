package com.sarthak.finance.controller;

import com.sarthak.finance.dto.response.MonthlyReportResponse;
import com.sarthak.finance.dto.response.YearlyReportResponse;
import com.sarthak.finance.exception.BadRequestException;
import com.sarthak.finance.model.User;
import com.sarthak.finance.security.CustomUserDetails;
import com.sarthak.finance.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reports", description = "Endpoints for generating monthly and yearly financial reports and analytics")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Get monthly financial report", description = "Generates monthly financial summary including total income, expenses, net savings, and category breakdown")
    @GetMapping({"/monthly/{year}/{month}", "/monthly"})
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable(name = "year", required = false) Integer pathYear,
            @PathVariable(name = "month", required = false) Integer pathMonth,
            @RequestParam(name = "year", required = false) Integer queryYear,
            @RequestParam(name = "month", required = false) Integer queryMonth,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int year = pathYear != null ? pathYear : (queryYear != null ? queryYear : 0);
        int month = pathMonth != null ? pathMonth : (queryMonth != null ? queryMonth : 0);

        if (year == 0 || month == 0) {
            throw new BadRequestException("Year and month are required");
        }

        User user = userDetails.getUser();
        MonthlyReportResponse report = reportService.getMonthlyReport(month, year, user);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Get yearly financial report", description = "Generates yearly financial summary including annual totals and month-by-month breakdown")
    @GetMapping({"/yearly/{year}", "/yearly"})
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @PathVariable(name = "year", required = false) Integer pathYear,
            @RequestParam(name = "year", required = false) Integer queryYear,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int year = pathYear != null ? pathYear : (queryYear != null ? queryYear : 0);

        if (year == 0) {
            throw new BadRequestException("Year is required");
        }

        User user = userDetails.getUser();
        YearlyReportResponse report = reportService.getYearlyReport(year, user);
        return ResponseEntity.ok(report);
    }
}
