package com.fluorineuck.minance.market.financial;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceSignalBundleTest {
    @Test
    void signalStrengthUsesDirectionAndConfidence() {
        PriceSignal signal = new PriceSignal(
                "equity:test",
                FinancialProductType.EQUITY,
                PriceSignalSource.FUNDAMENTAL,
                PriceSignalDirection.NEGATIVE,
                2.0D,
                0.25D,
                1200L,
                95L,
                4.0D,
                1.5D
        );

        assertEquals(-0.5D, signal.signedStrength());
        assertTrue(signal.hasAnchor());
        assertTrue(signal.hasLiquidity());
    }

    @Test
    void bundleAggregatesSignalsAndSelectsStrongestAnchor() {
        PriceSignal positive = new PriceSignal("fund:test", FinancialProductType.FUND, PriceSignalSource.ORDER_FLOW, PriceSignalDirection.POSITIVE, 3.0D, 0.5D, 0L, 0L, 5.0D, 0.0D);
        PriceSignal negative = new PriceSignal("fund:test", FinancialProductType.FUND, PriceSignalSource.RISK, PriceSignalDirection.NEGATIVE, 1.0D, 0.25D, 0L, 0L, 0.0D, 2.0D);
        FundamentalAnchor weakAnchor = new FundamentalAnchor("fund:test", FinancialProductType.FUND, FundamentalAnchorType.NAV, PriceSignalSource.FUNDAMENTAL, 90L, 0.4D, 0L);
        FundamentalAnchor strongAnchor = new FundamentalAnchor("fund:test", FinancialProductType.FUND, FundamentalAnchorType.INDEX_VALUE, PriceSignalSource.INDEX, 100L, 0.9D, 0L);

        PriceSignalBundle bundle = new PriceSignalBundle("fund:test", FinancialProductType.FUND, List.of(positive, negative), List.of(weakAnchor, strongAnchor));

        assertEquals(1.25D, bundle.signedStrength());
        assertEquals(5.0D, bundle.liquidityBid());
        assertEquals(2.0D, bundle.liquidityAsk());
        assertEquals(strongAnchor, bundle.strongestAnchor().orElseThrow());
    }

    @Test
    void bundleRejectsSignalsForDifferentProducts() {
        PriceSignal signal = new PriceSignal("option:test", FinancialProductType.OPTION, PriceSignalSource.PRODUCT_ADAPTER, PriceSignalDirection.POSITIVE, 1.0D, 1.0D, 0L, 0L, 0.0D, 0.0D);

        assertThrows(IllegalArgumentException.class, () -> new PriceSignalBundle("future:test", FinancialProductType.FUTURE, List.of(signal), List.of()));
    }
}
