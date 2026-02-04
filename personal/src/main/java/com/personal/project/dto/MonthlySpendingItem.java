package com.personal.project.dto;

public class MonthlySpendingItem {
    private final String month;
    private final double total;

    public MonthlySpendingItem(String month, double total) {
        this.month = month;
        this.total = total;
    }

    public String getMonth() {
        return month;
    }

    public double getTotal() {
        return total;
    }
}
