package com.fluorineuck.minance.product.fund;

import com.fluorineuck.minance.config.FinanceConfig;
import com.fluorineuck.minance.market.index.MarketIndexState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexTrackingFundAdapterTest {
    @Test
    void seedsInitialBasketFromIndexComponents() {
        FundState fund = new FundState("fund:test", "Fund Test", "manager", "tracking:test_index", 1_000.0D, 10.0D);
        MarketIndexState index = new MarketIndexState("test_index", "Test Index", 100L);
        index.update(100L, 2, List.of("minecraft:wheat", "minecraft:bread"), 10, 0L, 0L);
        FinanceConfig.FundRules rules = new FinanceConfig.FundRules(2, 0.5D, 0.02D);

        IndexTrackingFundAdapter.INSTANCE.seedInitialBasket(fund, index, rules, (productId, productType, fallbackPrice) -> "minecraft:wheat".equals(productId) ? 50L : 100L);

        assertEquals(2, fund.holdings().size());
        assertEquals(500.0D, fund.cash());
        assertEquals("minecraft:wheat", fund.holdings().getFirst().productId());
    }

    @Test
    void metricsEmitCreationRedemptionActionFromConfiguredThreshold() {
        FundState fund = new FundState("fund:test", "Fund Test", "manager", "tracking:test_index", 1_000.0D, 10.0D);
        fund.setSharePrice(120L);
        MarketIndexState index = new MarketIndexState("test_index", "Test Index", 100L);

        FundTrackingMetrics metrics = IndexTrackingFundAdapter.INSTANCE.metrics(fund, index, 0.02D);

        assertEquals(0.2D, metrics.premiumDiscount());
        assertEquals(FundCreationRedemptionAction.CREATION, metrics.creationRedemptionAction());
    }
}
