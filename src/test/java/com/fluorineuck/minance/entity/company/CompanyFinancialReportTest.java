package com.fluorineuck.minance.entity.company;

import com.fluorineuck.minance.config.CompanyConfig;
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
}
