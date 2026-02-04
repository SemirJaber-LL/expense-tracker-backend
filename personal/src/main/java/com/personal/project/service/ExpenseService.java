package com.personal.project.service;

import com.personal.project.dto.DashboardSummaryResponse;
import com.personal.project.dto.MonthlyReportItem;
import com.personal.project.dto.MonthlySpendingItem;
import com.personal.project.dto.ReportSummaryResponse;
import com.personal.project.dto.TypeBreakdownItem;
import com.personal.project.model.Expense;
import com.personal.project.repository.ExpenseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getExpenses(LocalDate from, LocalDate to, String category, String paymentMethodId, String type, String userId) {
        String normalizedCategory = normalize(category);
        String normalizedType = normalize(type);
        String normalizedPaymentMethodId = normalize(paymentMethodId);
        String normalizedUserId = normalize(userId);

        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' date must be on or before 'to' date");
        }

        return StreamSupport.stream(expenseRepository.findAll().spliterator(), false)
                .filter(expense -> matchesDateRange(expense, from, to))
                .filter(expense -> matchesIgnoreCase(expense.getCategory(), normalizedCategory))
                .filter(expense -> matchesIgnoreCase(expense.getType(), normalizedType))
                .filter(expense -> matchesExact(expense.getPaymentMethodId(), normalizedPaymentMethodId))
                .filter(expense -> matchesExact(expense.getUserId(), normalizedUserId))
                .collect(Collectors.toList());
    }

    public Expense getExpense(String id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteExpense(String id) {
        expenseRepository.deleteById(id);
    }

    public List<Expense> getExpensesByCategory(String category) {
        return expenseRepository.findByCategory(category);
    }

    public List<Expense> getExpensesByUserAndMonth(String userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public Optional<Expense> getExpenseById(String id) {
        return expenseRepository.findById(id);
    }
    @Transactional
    public Expense updateExpense(String id, Expense update, String callerUserId) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

        if (callerUserId != null && existing.getUserId() != null && !callerUserId.equals(existing.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this expense");
        }

        // copy updatable fields (adjust as needed for your domain)
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getAmount() != 0) existing.setAmount(update.getAmount());
       // if (update.getCurrency() != null) existing.setCurrency(update.getCurrency());
        if (update.getDate() != null) existing.setDate(update.getDate());
        if (update.getCategory() != null) existing.setCategory(update.getCategory());
        if (update.getType() != null) existing.setType(update.getType());
        if (update.getPaymentMethodId() != null) existing.setPaymentMethodId(update.getPaymentMethodId());
        if (update.getUserId() != null) existing.setUserId(update.getUserId());

        return expenseRepository.save(existing);
    }

    public List<String> getCategories() {
        return StreamSupport.stream(expenseRepository.findAll().spliterator(), false)
                .map(Expense::getCategory)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getCategoriesForUser(String userId) {
        return StreamSupport.stream(expenseRepository.findAll().spliterator(), false)
                .filter(e -> userId != null && userId.equals(e.getUserId()))
                .map(Expense::getCategory)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public DashboardSummaryResponse getDashboardSummary(LocalDate from, LocalDate to, String category, String paymentMethodId, String type, String userId) {
        List<Expense> expenses = getExpenses(from, to, category, paymentMethodId, type, userId);
        double totalSpent = 0;
        double debitTotal = 0;
        double creditTotal = 0;

        for (Expense expense : expenses) {
            double amount = normalizeAmount(expense);
            totalSpent += amount;
            if (isType(expense, "Debit")) {
                debitTotal += amount;
            } else if (isType(expense, "Credit")) {
                creditTotal += amount;
            }
        }

        return new DashboardSummaryResponse(totalSpent, debitTotal, creditTotal);
    }

    public List<TypeBreakdownItem> getTypeBreakdown(LocalDate from, LocalDate to, String category, String paymentMethodId, String type, String userId) {
        List<Expense> expenses = getExpenses(from, to, category, paymentMethodId, type, userId);
        Map<String, Double> totals = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Expense expense : expenses) {
            String key = normalize(expense.getType());
            if (key == null) {
                key = "Other";
            }
            totals.put(key, totals.getOrDefault(key, 0.0) + normalizeAmount(expense));
        }

        return totals.entrySet().stream()
                .map(entry -> new TypeBreakdownItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<MonthlySpendingItem> getMonthlySpending(int year, String category, String paymentMethodId, String type, String userId) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        List<Expense> expenses = getExpenses(from, to, category, paymentMethodId, type, userId);
        Map<Integer, Double> totals = new TreeMap<>();

        for (Expense expense : expenses) {
            LocalDate date = expense.getDate();
            if (date == null || date.getYear() != year) {
                continue;
            }
            int month = date.getMonthValue();
            totals.put(month, totals.getOrDefault(month, 0.0) + normalizeAmount(expense));
        }

        return buildMonthlySpendingItems(totals);
    }

    public ReportSummaryResponse getReportSummary(LocalDate from, LocalDate to, String category, String paymentMethodId, String userId) {
        List<Expense> expenses = getExpenses(from, to, category, paymentMethodId, null, userId);
        double totalSpent = 0;
        double totalIncome = 0;

        for (Expense expense : expenses) {
            double amount = expense.getAmount();
            if (amount < 0) {
                totalSpent += -amount;
            } else if (amount > 0) {
                totalIncome += amount;
            }
        }

        return new ReportSummaryResponse(totalSpent, totalIncome, totalIncome - totalSpent);
    }

    public List<MonthlyReportItem> getMonthlyReport(LocalDate from, LocalDate to, String category, String paymentMethodId, String userId) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' and 'to' dates are required");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' date must be on or before 'to' date");
        }

        List<Expense> expenses = getExpenses(from, to, category, paymentMethodId, null, userId);
        Map<YearMonth, double[]> totals = new TreeMap<>();

        for (Expense expense : expenses) {
            LocalDate date = expense.getDate();
            if (date == null || date.isBefore(from) || date.isAfter(to)) {
                continue;
            }
            YearMonth month = YearMonth.from(date);
            double[] values = totals.computeIfAbsent(month, ignored -> new double[2]);
            double amount = expense.getAmount();
            if (amount < 0) {
                values[0] += -amount;
            } else if (amount > 0) {
                values[1] += amount;
            }
        }

        return totals.entrySet().stream()
                .map(entry -> {
                    String label = entry.getKey().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    return new MonthlyReportItem(label, entry.getValue()[0], entry.getValue()[1]);
                })
                .collect(Collectors.toList());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean matchesIgnoreCase(String actual, String expected) {
        return expected == null || (actual != null && actual.equalsIgnoreCase(expected));
    }

    private boolean matchesExact(String actual, String expected) {
        return expected == null || (actual != null && actual.equals(expected));
    }

    private boolean matchesDateRange(Expense expense, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        LocalDate date = expense.getDate();
        if (date == null) {
            return false;
        }
        if (from != null && date.isBefore(from)) {
            return false;
        }
        if (to != null && date.isAfter(to)) {
            return false;
        }
        return true;
    }

    private double normalizeAmount(Expense expense) {
        double amount = expense.getAmount();
        return amount < 0 ? -amount : amount;
    }

    private boolean isType(Expense expense, String expected) {
        return expense.getType() != null && expense.getType().equalsIgnoreCase(expected);
    }

    private List<MonthlySpendingItem> buildMonthlySpendingItems(Map<Integer, Double> totals) {
        return java.util.stream.Stream.of(Month.values())
                .map(month -> {
                    int monthValue = month.getValue();
                    String label = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    double total = totals.getOrDefault(monthValue, 0.0);
                    return new MonthlySpendingItem(label, total);
                })
                .collect(Collectors.toList());
    }



    // Add other methods as needed
}
