package com.fluorineuck.minance.entity.company;

import net.minecraft.nbt.CompoundTag;

public record CompanyOperatingStatement(
        long revenue,
        long operatingCost,
        double grossMargin,
        double operatingMargin,
        long netIncome,
        long cashFlow,
        long retainedEarnings
) {
    public CompanyOperatingStatement {
        revenue = Math.max(0L, revenue);
        operatingCost = Math.max(0L, operatingCost);
        grossMargin = clamp(grossMargin);
        operatingMargin = clamp(operatingMargin);
        netIncome = Math.max(0L, netIncome);
        cashFlow = Math.max(0L, cashFlow);
        retainedEarnings = Math.max(0L, retainedEarnings);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("revenue", revenue);
        tag.putLong("operating_cost", operatingCost);
        tag.putDouble("gross_margin", grossMargin);
        tag.putDouble("operating_margin", operatingMargin);
        tag.putLong("net_income", netIncome);
        tag.putLong("cash_flow", cashFlow);
        tag.putLong("retained_earnings", retainedEarnings);
        return tag;
    }

    public static CompanyOperatingStatement load(CompoundTag tag) {
        return new CompanyOperatingStatement(
                tag.getLong("revenue"),
                tag.getLong("operating_cost"),
                tag.getDouble("gross_margin"),
                tag.getDouble("operating_margin"),
                tag.getLong("net_income"),
                tag.getLong("cash_flow"),
                tag.getLong("retained_earnings")
        );
    }

    static CompanyOperatingStatement fromLegacy(long openingFunds, long periodIncome, long closingFunds) {
        long revenue = Math.max(0L, periodIncome);
        long operatingCost = 0L;
        long netIncome = Math.max(0L, revenue - operatingCost);
        double margin = ratio(netIncome, revenue);
        long retained = Math.max(0L, closingFunds - openingFunds);
        return new CompanyOperatingStatement(revenue, operatingCost, margin, margin, netIncome, netIncome, retained);
    }

    static double ratio(double numerator, double denominator) {
        if (denominator <= 0.0D) {
            return 0.0D;
        }
        return numerator / denominator;
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
