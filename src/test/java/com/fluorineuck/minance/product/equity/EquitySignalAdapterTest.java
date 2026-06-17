package com.fluorineuck.minance.product.equity;

import com.fluorineuck.minance.entity.company.CompanyFinancialReport;
import com.fluorineuck.minance.entity.company.CompanyBalanceSheet;
import com.fluorineuck.minance.entity.company.CompanyFinancialMetrics;
import com.fluorineuck.minance.entity.company.CompanyOperatingStatement;
import com.fluorineuck.minance.market.financial.FundamentalAnchorType;
import com.fluorineuck.minance.market.financial.PriceSignalBundle;
import com.fluorineuck.minance.market.financial.PriceSignalDirection;
import com.fluorineuck.minance.market.financial.PriceSignalSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquitySignalAdapterTest {
    @Test
    void reportCreatesNavAnchorAndPositiveEarningsSignal() {
        EquityAsset asset = new EquityAsset("EQ_company_common_stock", "company", "Company common_stock", EquityAssetType.COMMON_STOCK, 100, 80L, true);
        CompanyFinancialReport report = report(120L, 0.20D, true);

        PriceSignalBundle bundle = EquityMarketService.INSTANCE.priceSignals(asset, report);

        assertEquals(1, bundle.anchors().size());
        assertEquals(FundamentalAnchorType.NAV, bundle.anchors().getFirst().anchorType());
        assertEquals(120L, bundle.anchors().getFirst().anchorPrice());
        assertEquals(1, bundle.signals().size());
        assertEquals(PriceSignalSource.EARNINGS, bundle.signals().getFirst().source());
        assertEquals(PriceSignalDirection.POSITIVE, bundle.signals().getFirst().direction());
        assertTrue(bundle.signedStrength() > 0.0D);
    }

    @Test
    void negativePerformanceCreatesNegativeSignal() {
        EquityAsset asset = new EquityAsset("EQ_company_common_stock", "company", "Company common_stock", EquityAssetType.COMMON_STOCK, 100, 80L, true);
        CompanyFinancialReport report = report(90L, -0.15D, true);

        PriceSignalBundle bundle = EquityMarketService.INSTANCE.priceSignals(asset, report);

        assertEquals(1, bundle.signals().size());
        assertEquals(PriceSignalDirection.NEGATIVE, bundle.signals().getFirst().direction());
        assertTrue(bundle.signedStrength() < 0.0D);
    }

    @Test
    void insignificantReportOnlyCreatesAnchor() {
        EquityAsset asset = new EquityAsset("EQ_company_common_stock", "company", "Company common_stock", EquityAssetType.COMMON_STOCK, 100, 80L, true);
        CompanyFinancialReport report = report(100L, 0.03D, false);

        PriceSignalBundle bundle = EquityMarketService.INSTANCE.priceSignals(asset, report);

        assertEquals(1, bundle.anchors().size());
        assertEquals(0, bundle.signals().size());
    }

    @Test
    void earningsGrowthMetricOverridesLegacyPerformanceSignal() {
        EquityAsset asset = new EquityAsset("EQ_company_common_stock", "company", "Company common_stock", EquityAssetType.COMMON_STOCK, 100, 80L, true);
        CompanyOperatingStatement operating = new CompanyOperatingStatement(500L, 0L, 1.0D, 1.0D, 500L, 500L, 4_000L);
        CompanyBalanceSheet balance = new CompanyBalanceSheet(12_000L, 0L, 0L, 0L, 0L, 12_000L, 120L);
        CompanyFinancialMetrics metrics = new CompanyFinancialMetrics(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.04D, 0.04D, 1.0D, -0.30D, -0.20D);
        CompanyFinancialReport report = new CompanyFinancialReport("company", 0L, 2_400L, 8_000L, 500L, 12_000L, 100, 120L, 0.0D, 0.0D, 0.30D, 0.20D, operating, balance, metrics, true);

        PriceSignalBundle bundle = EquityMarketService.INSTANCE.priceSignals(asset, report);

        assertEquals(PriceSignalDirection.NEGATIVE, bundle.signals().getFirst().direction());
        assertTrue(bundle.signedStrength() < 0.0D);
    }

    private static CompanyFinancialReport report(long navPerShare, double performanceChangeRatio, boolean significant) {
        return new CompanyFinancialReport("company", 0L, 2_400L, 8_000L, 500L, 12_000L, 100, navPerShare, 0.0D, 0.0D, Math.abs(performanceChangeRatio), performanceChangeRatio, significant);
    }
}
