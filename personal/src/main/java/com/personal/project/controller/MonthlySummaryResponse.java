package com.personal.project.controller;

import com.personal.project.model.Expense;

import java.util.List;

public class MonthlySummaryResponse {
    private final double total;
    private final List<Expense> expenses;

    public MonthlySummaryResponse(double total, List<Expense> expenses) {
        this.total = total;
        this.expenses = expenses;
    }

    public double getTotal() {
        return total;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }
}
