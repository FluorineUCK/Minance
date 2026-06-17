package com.fluorineuck.minance.entity.company;

import com.fluorineuck.minance.config.CompanyConfig;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyFinancialReportTest {
    @Test
    void stableReportsDoNotKeepAddingAttraction() {
        CompanyConfig config = CompanyConfig.defaults();
        CompanyFinancialReport initial = CompanyFinancialReport.initial("company", 0L, 56_000L, 112);
        CompanyFinancialReport first = CompanyFinancialReport.publish("company", 0L, 2_400L, 56_000L, 4_080L, 60_080L, 112, 47.78D, 0.0D, initial, config);
        CompanyFinancialReport second = CompanyFinancialReport.publish("company", 2_400L, 4_800L, 60_080L, 4_080L, 64_160L, 112, 47.78D, 0.0D, first, config);

        assertTrue(first.significantChange());
        assertTrue(first.attractionVolume(config) > 0);
        assertFalse(second.significantChange());
        assertEquals(0, second.attractionVolume(config));
    }

    @Test
    void largeReportChangeAddsAttraction() {
        CompanyConfig config = CompanyConfig.defaults();
        CompanyFinancialReport previous = CompanyFinancialReport.publish("company", 0L, 2_400L, 56_000L, 4_080L, 60_080L, 112, 47.78D, 0.0D, CompanyFinancialReport.initial("company", 0L, 56_000L, 112), config);
        CompanyFinancialReport report = CompanyFinancialReport.publish("company", 2_400L, 4_800L, 60_080L, 16_000L, 76_080L, 112, 47.78D, 0.0D, previous, config);

        assertTrue(report.significantChange());
        assertTrue(report.positiveSurprise());
        assertTrue(report.attractionVolume(config) > 0);
    }

    @Test
    void publishedReportIncludesAccountingStatementsAndMetrics() {
        CompanyConfig config = CompanyConfig.defaults();
        CompanyFinancialReport previous = CompanyFinancialReport.publish("company", 0L, 2_400L, 56_000L, 4_080L, 60_080L, 112, 47.78D, 0.0D, CompanyFinancialReport.initial("company", 0L, 56_000L, 112), config);
        CompanyFinancialReport report = CompanyFinancialReport.publish("company", 2_400L, 4_800L, 60_080L, 4_080L, 64_160L, 112, 47.78D, 0.0D, previous, config);

        assertEquals(4_080L, report.operatingStatement().revenue());
        assertEquals(4_080L, report.operatingStatement().netIncome());
        assertEquals(4_080L, report.operatingStatement().cashFlow());
        assertEquals(64_160L, report.balanceSheet().cash());
        assertEquals(64_160L, report.balanceSheet().netAssetValue());
        assertEquals(report.navPerShare(), report.balanceSheet().bookValuePerShare());
        assertEquals(0.0D, report.metrics().debtToAssets());
        assertEquals(0.0D, report.metrics().defaultPressure());
        assertEquals(0.0D, report.metrics().earningsGrowth());
        assertTrue(report.metrics().returnOnAssets() > 0.0D);
    }

    @Test
    void saveLoadPreservesAccountingStatementsAndMetrics() {
        CompanyConfig config = CompanyConfig.defaults();
        CompanyFinancialReport report = CompanyFinancialReport.publish("company", 0L, 2_400L, 56_000L, 4_080L, 60_080L, 112, 47.78D, 0.0D, CompanyFinancialReport.initial("company", 0L, 56_000L, 112), config);

        CompanyFinancialReport loaded = CompanyFinancialReport.load(report.save());

        assertEquals(report.operatingStatement(), loaded.operatingStatement());
        assertEquals(report.balanceSheet(), loaded.balanceSheet());
        assertEquals(report.metrics(), loaded.metrics());
    }

    @Test
    void legacyReportLoadDerivesAccountingStatements() {
        CompoundTag tag = new CompoundTag();
        tag.putString("company_id", "company");
        tag.putLong("start_tick", 0L);
        tag.putLong("end_tick", 2_400L);
        tag.putLong("opening_funds", 56_000L);
        tag.putLong("period_income", 4_080L);
        tag.putLong("closing_funds", 60_080L);
        tag.putInt("total_shares", 112);
        tag.putLong("nav_per_share", 536L);
        tag.putDouble("average_proxy_liquidity_demand", 47.78D);
        tag.putDouble("average_derivative_demand", 0.0D);
        tag.putDouble("report_change_ratio", 0.08D);
        tag.putDouble("performance_change_ratio", 0.08D);
        tag.putBoolean("significant_change", true);

        CompanyFinancialReport loaded = CompanyFinancialReport.load(tag);

        assertEquals(4_080L, loaded.operatingStatement().revenue());
        assertEquals(60_080L, loaded.balanceSheet().cash());
        assertEquals(536L, loaded.navPerShare());
        assertEquals(0.0D, loaded.metrics().debtToEquity());
    }
}
