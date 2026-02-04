package com.personal.project.controller;

import com.personal.project.dto.DashboardSummaryResponse;
import com.personal.project.dto.MonthlySpendingItem;
import com.personal.project.dto.TypeBreakdownItem;
import com.personal.project.model.Expense;
import com.personal.project.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public Expense createExpense(@RequestBody Expense expense) {
        return expenseService.addExpense(expense);
    }

    @GetMapping
    public List<Expense> getAllExpenses(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String paymentMethodId,
                                        @RequestParam(required = false) String type,
                                        @RequestParam(required = false) String userId) {
        return expenseService.getExpenses(from, to, category, paymentMethodId, type, userId);
    }

    @GetMapping("/{id}")
    public Expense getExpense(@PathVariable String id) {
        return expenseService.getExpense(id);
    }

    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable String id,
                                 @RequestBody Expense expense,
                                 @RequestParam(required = false) String userId) {
        return expenseService.updateExpense(id, expense, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<?> getMonthlySummary(@RequestParam String userId, @RequestParam int year, @RequestParam int month) {
        List<Expense> expenses = expenseService.getExpensesByUserAndMonth(userId, year, month);
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        return ResponseEntity.ok(new MonthlySummaryResponse(total, expenses));
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                               @RequestParam(required = false) String category,
                                               @RequestParam(required = false) String paymentMethodId,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) String userId) {
        return expenseService.getDashboardSummary(from, to, category, paymentMethodId, type, userId);
    }

    @GetMapping("/breakdown")
    public List<TypeBreakdownItem> getTypeBreakdown(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                    @RequestParam(required = false) String category,
                                                    @RequestParam(required = false) String paymentMethodId,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(required = false) String userId) {
        return expenseService.getTypeBreakdown(from, to, category, paymentMethodId, type, userId);
    }

    @GetMapping("/monthly-spending")
    public List<MonthlySpendingItem> getMonthlySpending(@RequestParam int year,
                                                        @RequestParam(required = false) String category,
                                                        @RequestParam(required = false) String paymentMethodId,
                                                        @RequestParam(required = false) String type,
                                                        @RequestParam(required = false) String userId) {
        return expenseService.getMonthlySpending(year, category, paymentMethodId, type, userId);
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<Expense>> getByCategory(@RequestParam("category") String category) {
        List<Expense> expenses;
        expenses = expenseService.getExpensesByCategory(category);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(@RequestParam(required = false) String userId) {
        List<String> categories = userId == null
                ? expenseService.getCategories()
                : expenseService.getCategoriesForUser(userId);
        return ResponseEntity.ok(categories);
    }
}
