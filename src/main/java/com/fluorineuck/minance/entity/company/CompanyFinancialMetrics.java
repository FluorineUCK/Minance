package com.fluorineuck.minance.entity.company;

import net.minecraft.nbt.CompoundTag;

public record CompanyFinancialMetrics(
        double debtToAssets,
        double debtToEquity,
        double interestCoverage,
        double currentRatio,
        double defaultPressure,
        double returnOnAssets,
        double returnOnEquity,
        double profitMargin,
        double earningsGrowth,
        double cashFlowGrowth
) {
    public CompanyFinancialMetrics {
        debtToAssets = nonNegative(debtToAssets);
        debtToEquity = nonNegative(debtToEquity);
        interestCoverage = nonNegative(interestCoverage);
        currentRatio = nonNegative(currentRatio);
        defaultPressure = clamp(defaultPressure);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("debt_to_assets", debtToAssets);
        tag.putDouble("debt_to_equity", debtToEquity);
        tag.putDouble("interest_coverage", interestCoverage);
        tag.putDouble("current_ratio", currentRatio);
        tag.putDouble("default_pressure", defaultPressure);
        tag.putDouble("return_on_assets", returnOnAssets);
        tag.putDouble("return_on_equity", returnOnEquity);
        tag.putDouble("profit_margin", profitMargin);
        tag.putDouble("earnings_growth", earningsGrowth);
        tag.putDouble("cash_flow_growth", cashFlowGrowth);
        return tag;
    }

    public static CompanyFinancialMetrics load(CompoundTag tag) {
        return new CompanyFinancialMetrics(
                tag.getDouble("debt_to_assets"),
                tag.getDouble("debt_to_equity"),
                tag.getDouble("interest_coverage"),
                tag.getDouble("current_ratio"),
                tag.getDouble("default_pressure"),
                tag.getDouble("return_on_assets"),
                tag.getDouble("return_on_equity"),
                tag.getDouble("profit_margin"),
                tag.getDouble("earnings_growth"),
                tag.getDouble("cash_flow_growth")
        );
    }

    static CompanyFinancialMetrics fromStatements(CompanyOperatingStatement operating, CompanyBalanceSheet balance, CompanyFinancialReport previous) {
        long totalAssets = balance.totalAssets();
        long equity = balance.equityValue();
        long liabilities = balance.liabilities();
        long currentAssets = balance.cash() + balance.inventoryValue();
        double debtToAssets = ratio(liabilities, totalAssets);
        double debtToEquity = ratio(liabilities, equity);
        double interestCoverage = liabilities > 0L ? ratio(operating.netIncome(), liabilities) : 0.0D;
        double currentRatio = ratio(currentAssets, liabilities);
        double defaultPressure = debtToAssets;
        double returnOnAssets = ratio(operating.netIncome(), totalAssets);
        double returnOnEquity = ratio(operating.netIncome(), equity);
        double profitMargin = CompanyOperatingStatement.ratio(operating.netIncome(), operating.revenue());
        double earningsGrowth = previous == null ? 0.0D : relativeChange(operating.netIncome(), previous.operatingStatement().netIncome());
        double cashFlowGrowth = previous == null ? 0.0D : relativeChange(operating.cashFlow(), previous.operatingStatement().cashFlow());
        return new CompanyFinancialMetrics(debtToAssets, debtToEquity, interestCoverage, currentRatio, defaultPressure, returnOnAssets, returnOnEquity, profitMargin, earningsGrowth, cashFlowGrowth);
    }

    private static double ratio(double numerator, double denominator) {
        if (denominator <= 0.0D) {
            return 0.0D;
        }
        return numerator / denominator;
    }

    private static double relativeChange(double current, double previous) {
        double basis = Math.max(1.0D, Math.abs(previous));
        return (current - previous) / basis;
    }

    private static double nonNegative(double value) {
        return Math.max(0.0D, value);
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
