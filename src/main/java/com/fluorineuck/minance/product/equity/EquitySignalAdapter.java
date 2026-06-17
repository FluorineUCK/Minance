package com.fluorineuck.minance.product.equity;

import com.fluorineuck.minance.config.FinanceConfig;
import com.fluorineuck.minance.entity.company.CompanyFinancialReport;
import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.market.financial.FundamentalAnchor;
import com.fluorineuck.minance.market.financial.FundamentalAnchorType;
import com.fluorineuck.minance.market.financial.PriceSignal;
import com.fluorineuck.minance.market.financial.PriceSignalBundle;
import com.fluorineuck.minance.market.financial.PriceSignalDirection;
import com.fluorineuck.minance.market.financial.PriceSignalSource;

import java.util.ArrayList;
import java.util.List;

final class EquitySignalAdapter {
    PriceSignalBundle fromReport(EquityAsset asset, CompanyFinancialReport report, long fallbackNavPerShare, FinanceConfig.EquitySignalRules config) {
        String productId = asset.id();
        long anchorPrice = report == null ? fallbackNavPerShare : report.navPerShare();
        FundamentalAnchor anchor = new FundamentalAnchor(
                productId,
                FinancialProductType.EQUITY,
                FundamentalAnchorType.NAV,
                PriceSignalSource.FUNDAMENTAL,
                anchorPrice,
                config.navAnchorConfidence(),
                config.signalHorizonTicks()
        );

        List<PriceSignal> signals = new ArrayList<>();
        if (report != null && report.significantChange() && reportPerformance(report) != 0.0D) {
            double performance = reportPerformance(report);
            signals.add(new PriceSignal(
                    productId,
                    FinancialProductType.EQUITY,
                    PriceSignalSource.EARNINGS,
                    performance > 0.0D ? PriceSignalDirection.POSITIVE : PriceSignalDirection.NEGATIVE,
                    Math.min(Math.abs(performance) * Math.max(0.0D, config.performanceSignalMultiplier()), Math.max(0.0D, config.maxSignalStrength())),
                    config.performanceSignalConfidence(),
                    config.signalHorizonTicks(),
                    anchorPrice,
                    0.0D,
                    0.0D
            ));
        }

        return new PriceSignalBundle(productId, FinancialProductType.EQUITY, signals, List.of(anchor));
    }

    private static double reportPerformance(CompanyFinancialReport report) {
        double earningsGrowth = report.metrics().earningsGrowth();
        return earningsGrowth == 0.0D ? report.performanceChangeRatio() : earningsGrowth;
    }
}
