package com.personal.project.dto;

public class ReportSummaryResponse {
    private final double totalSpent;
    private final double totalIncome;
    private final double net;

    public ReportSummaryResponse(double totalSpent, double totalIncome, double net) {
        this.totalSpent = totalSpent;
        this.totalIncome = totalIncome;
        this.net = net;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getNet() {
        return net;
    }
}
