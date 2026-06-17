package com.fluorineuck.minance.entity.company;

import com.fluorineuck.minance.config.CompanyConfig;
import com.fluorineuck.minance.config.ConfigRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record CompanyFinancialReport(
        String companyId,
        long startTick,
        long endTick,
        long openingFunds,
        long periodIncome,
        long closingFunds,
        int totalShares,
        long navPerShare,
        double averageProxyLiquidityDemand,
        double averageDerivativeDemand,
        double reportChangeRatio,
        double performanceChangeRatio,
        CompanyOperatingStatement operatingStatement,
        CompanyBalanceSheet balanceSheet,
        CompanyFinancialMetrics metrics,
        boolean significantChange
) {
    public CompanyFinancialReport(
            String companyId,
            long startTick,
            long endTick,
            long openingFunds,
            long periodIncome,
            long closingFunds,
            int totalShares,
            long navPerShare,
            double averageProxyLiquidityDemand,
            double averageDerivativeDemand,
            double reportChangeRatio,
            double performanceChangeRatio,
            boolean significantChange
    ) {
        this(
                companyId,
                startTick,
                endTick,
                openingFunds,
                periodIncome,
                closingFunds,
                totalShares,
                navPerShare,
                averageProxyLiquidityDemand,
                averageDerivativeDemand,
                reportChangeRatio,
                performanceChangeRatio,
                CompanyAccountingSnapshot.fromReportInputs(openingFunds, periodIncome, closingFunds, totalShares, null).operatingStatement(),
                CompanyAccountingSnapshot.fromReportInputs(openingFunds, periodIncome, closingFunds, totalShares, null).balanceSheet(),
                CompanyAccountingSnapshot.fromReportInputs(openingFunds, periodIncome, closingFunds, totalShares, null).metrics(),
                significantChange
        );
    }

    public CompanyFinancialReport {
        operatingStatement = operatingStatement == null
                ? CompanyAccountingSnapshot.fromReportInputs(openingFunds, periodIncome, closingFunds, totalShares, null).operatingStatement()
                : operatingStatement;
        balanceSheet = balanceSheet == null
                ? CompanyAccountingSnapshot.fromReportInputs(openingFunds, periodIncome, closingFunds, totalShares, null).balanceSheet()
                : balanceSheet;
        metrics = metrics == null
                ? CompanyFinancialMetrics.fromStatements(operatingStatement, balanceSheet, null)
                : metrics;
    }

    public static CompanyFinancialReport initial(String companyId, long tick, long funds, int totalShares) {
        long shares = Math.max(1, totalShares);
        long nav = Math.max(1L, Math.round(Math.max(0L, funds) / (double) shares));
        CompanyAccountingSnapshot snapshot = CompanyAccountingSnapshot.fromReportInputs(Math.max(0L, funds), 0L, Math.max(0L, funds), Math.max(1, totalShares), null);
        return new CompanyFinancialReport(companyId, tick, tick, Math.max(0L, funds), 0L, Math.max(0L, funds), Math.max(1, totalShares), nav, 0.0D, 0.0D, 0.0D, 0.0D, snapshot.operatingStatement(), snapshot.balanceSheet(), snapshot.metrics(), false);
    }

    public static CompanyFinancialReport publish(
            String companyId,
            long startTick,
            long endTick,
            long openingFunds,
            long periodIncome,
            long closingFunds,
            int totalShares,
            double averageProxyLiquidityDemand,
            double averageDerivativeDemand,
            CompanyFinancialReport previous,
            CompanyConfig config
    ) {
        int shares = Math.max(1, totalShares);
        long nav = Math.max(1L, Math.round(Math.max(0L, closingFunds) / (double) shares));
        double proxyDemand = Math.max(0.0D, averageProxyLiquidityDemand);
        double derivativeDemand = Math.max(0.0D, averageDerivativeDemand);
        double changeRatio = previous == null ? 0.0D : reportChangeRatio(nav, periodIncome, proxyDemand + derivativeDemand, previous, config);
        double performanceChange = previous == null ? 0.0D : performanceChangeRatio(nav, periodIncome, previous, config);
        boolean significant = previous != null && changeRatio >= Math.max(0.0D, config.report().significantChangeThreshold());
        CompanyAccountingSnapshot snapshot = CompanyAccountingSnapshot.fromReportInputs(openingFunds, periodIncome, closingFunds, shares, previous);
        return new CompanyFinancialReport(
                companyId,
                startTick,
                endTick,
                Math.max(0L, openingFunds),
                Math.max(0L, periodIncome),
                Math.max(0L, closingFunds),
                shares,
                nav,
                proxyDemand,
                derivativeDemand,
                changeRatio,
                performanceChange,
                snapshot.operatingStatement(),
                snapshot.balanceSheet(),
                snapshot.metrics(),
                significant
        );
    }

    public double totalLiquidityDemand() {
        return Math.max(0.0D, averageProxyLiquidityDemand) + Math.max(0.0D, averageDerivativeDemand);
    }

    public int attractionVolume(CompanyConfig config) {
        if (!significantChange) {
            return 0;
        }
        double demandBasis = Math.max(config.report().attractionDemandFloor(), totalLiquidityDemand());
        double signal = Math.max(0.0D, reportChangeRatio) * Math.max(0.0D, config.report().attractionMultiplier());
        return Math.max(Math.max(0, config.report().minimumAttractionVolume()), (int) Math.round(demandBasis * signal));
    }

    public boolean positiveSurprise() {
        return performanceChangeRatio >= 0.0D;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("company_id", companyId);
        tag.putLong("start_tick", startTick);
        tag.putLong("end_tick", endTick);
        tag.putLong("opening_funds", openingFunds);
        tag.putLong("period_income", periodIncome);
        tag.putLong("closing_funds", closingFunds);
        tag.putInt("total_shares", totalShares);
        tag.putLong("nav_per_share", navPerShare);
        tag.putDouble("average_proxy_liquidity_demand", averageProxyLiquidityDemand);
        tag.putDouble("average_derivative_demand", averageDerivativeDemand);
        tag.putDouble("report_change_ratio", reportChangeRatio);
        tag.putDouble("performance_change_ratio", performanceChangeRatio);
        tag.put("operating_statement", operatingStatement.save());
        tag.put("balance_sheet", balanceSheet.save());
        tag.put("financial_metrics", metrics.save());
        tag.putBoolean("significant_change", significantChange);
        return tag;
    }

    public static CompanyFinancialReport load(CompoundTag tag) {
        long closingFunds = tag.getLong("closing_funds");
        int totalShares = Math.max(1, tag.getInt("total_shares"));
        long nav = tag.contains("nav_per_share", Tag.TAG_LONG)
                ? Math.max(1L, tag.getLong("nav_per_share"))
                : Math.max(1L, Math.round(closingFunds / (double) totalShares));
        long openingFunds = Math.max(0L, tag.getLong("opening_funds"));
        long periodIncome = Math.max(0L, tag.getLong("period_income"));
        CompanyAccountingSnapshot fallback = CompanyAccountingSnapshot.fromReportInputs(openingFunds, periodIncome, closingFunds, totalShares, null);
        CompanyOperatingStatement operating = tag.contains("operating_statement", Tag.TAG_COMPOUND)
                ? CompanyOperatingStatement.load(tag.getCompound("operating_statement"))
                : fallback.operatingStatement();
        CompanyBalanceSheet balance = tag.contains("balance_sheet", Tag.TAG_COMPOUND)
                ? CompanyBalanceSheet.load(tag.getCompound("balance_sheet"))
                : fallback.balanceSheet();
        CompanyFinancialMetrics metrics = tag.contains("financial_metrics", Tag.TAG_COMPOUND)
                ? CompanyFinancialMetrics.load(tag.getCompound("financial_metrics"))
                : CompanyFinancialMetrics.fromStatements(operating, balance, null);
        return new CompanyFinancialReport(
                tag.getString("company_id"),
                tag.getLong("start_tick"),
                tag.getLong("end_tick"),
                openingFunds,
                periodIncome,
                Math.max(0L, closingFunds),
                totalShares,
                nav,
                Math.max(0.0D, tag.getDouble("average_proxy_liquidity_demand")),
                Math.max(0.0D, tag.getDouble("average_derivative_demand")),
                Math.max(0.0D, tag.getDouble("report_change_ratio")),
                clamp(tag.getDouble("performance_change_ratio"), ConfigRegistry.INSTANCE.company().report().minPerformanceChangeRatio(), ConfigRegistry.INSTANCE.company().report().maxPerformanceChangeRatio()),
                operating,
                balance,
                metrics,
                tag.getBoolean("significant_change")
        );
    }

    private static double reportChangeRatio(long nav, long income, double demand, CompanyFinancialReport previous, CompanyConfig config) {
        double navChange = Math.abs(relativeChange(nav, previous.navPerShare()));
        double incomeChange = Math.abs(relativeChange(income, previous.periodIncome()));
        double demandChange = Math.abs(relativeChange(demand, previous.totalLiquidityDemand()));
        return clamp(Math.max(navChange, Math.max(incomeChange, demandChange)), config.report().minReportChangeRatio(), config.report().maxReportChangeRatio());
    }

    private static double performanceChangeRatio(long nav, long income, CompanyFinancialReport previous, CompanyConfig config) {
        double navChange = relativeChange(nav, previous.navPerShare());
        double incomeChange = relativeChange(income, previous.periodIncome());
        return clamp(navChange * config.report().performanceNavWeight() + incomeChange * config.report().performanceIncomeWeight(), config.report().minPerformanceChangeRatio(), config.report().maxPerformanceChangeRatio());
    }

    private static double relativeChange(double current, double previous) {
        double basis = Math.max(1.0D, Math.abs(previous));
        return (current - previous) / basis;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
