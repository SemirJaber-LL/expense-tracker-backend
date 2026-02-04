package com.personal.project.dto;

public class MonthlyReportItem {
    private final String month;
    private final double spent;
    private final double income;

    public MonthlyReportItem(String month, double spent, double income) {
        this.month = month;
        this.spent = spent;
        this.income = income;
    }

    public String getMonth() {
        return month;
    }

    public double getSpent() {
        return spent;
    }

    public double getIncome() {
        return income;
    }
}
