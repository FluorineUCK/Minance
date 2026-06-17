package com.fluorineuck.minance.market.financial;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialMarketEngineSignalTest {
    @AfterEach
    void clearMarkets() {
        FinancialMarketEngine.INSTANCE.markets().clear();
    }

    @Test
    void updateConsumesSignalBundleLiquidity() {
        PriceSignal signal = new PriceSignal(
                "equity:test",
                FinancialProductType.EQUITY,
                PriceSignalSource.EARNINGS,
                PriceSignalDirection.POSITIVE,
                0.0D,
                1.0D,
                0L,
                100L,
                12.0D,
                0.0D
        );
        PriceSignalBundle bundle = new PriceSignalBundle("equity:test", FinancialProductType.EQUITY, List.of(signal), List.of());

        FinancialMarketResult result = FinancialMarketEngine.INSTANCE.update("equity:test", FinancialProductType.EQUITY, 100L, 0.01D, 0, 100L, bundle);

        assertTrue(result.stats().generatedBuyLiquidity() >= 12.0D);
    }

    @Test
    void updateRejectsMismatchedSignalBundle() {
        PriceSignalBundle bundle = PriceSignalBundle.empty("fund:test", FinancialProductType.FUND);

        assertThrows(IllegalArgumentException.class, () -> FinancialMarketEngine.INSTANCE.update("equity:test", FinancialProductType.EQUITY, 100L, 0.01D, 0, 100L, bundle));
    }
}
