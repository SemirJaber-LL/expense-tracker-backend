package com.personal.project.controller;

import com.personal.project.dto.MonthlyReportItem;
import com.personal.project.dto.ReportSummaryResponse;
import com.personal.project.service.ExpenseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ExpenseService expenseService;

    public ReportController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/summary")
    public ReportSummaryResponse getSummary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) String paymentMethodId,
                                            @RequestParam(required = false) String userId) {
        return expenseService.getReportSummary(from, to, category, paymentMethodId, userId);
    }

    @GetMapping("/monthly")
    public List<MonthlyReportItem> getMonthly(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) String paymentMethodId,
                                              @RequestParam(required = false) String userId) {
        return expenseService.getMonthlyReport(from, to, category, paymentMethodId, userId);
    }
}
