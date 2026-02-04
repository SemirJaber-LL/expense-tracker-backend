package com.personal.project.dto;

public class DashboardSummaryResponse {
    private final double totalSpent;
    private final double debitTotal;
    private final double creditTotal;

    public DashboardSummaryResponse(double totalSpent, double debitTotal, double creditTotal) {
        this.totalSpent = totalSpent;
        this.debitTotal = debitTotal;
        this.creditTotal = creditTotal;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public double getDebitTotal() {
        return debitTotal;
    }

    public double getCreditTotal() {
        return creditTotal;
    }
}
