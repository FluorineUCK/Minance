package com.fluorineuck.minance.product.component.fund;

import com.fluorineuck.minance.config.FinanceConfig;
import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.market.index.MarketIndexState;

import java.util.List;

final class IndexTrackingFundAdapter {
    static final IndexTrackingFundAdapter INSTANCE = new IndexTrackingFundAdapter();

    private IndexTrackingFundAdapter() {
    }

    void seedInitialBasket(FundState fund, MarketIndexState index, FinanceConfig.FundRules config, FundState.ProductPriceResolver priceResolver) {
        if (fund == null || index == null || priceResolver == null) {
            return;
        }
        List<String> componentIds = index.componentIds().stream()
                .limit(Math.max(0, config.indexTrackingMaxEquities()))
                .toList();
        if (componentIds.isEmpty()) {
            return;
        }
        double allocation = fund.cash() * config.indexTrackingInitialAllocationRatio();
        double componentAllocation = allocation / componentIds.size();
        for (String componentId : componentIds) {
            long price = priceResolver.resolvePrice(componentId, FinancialProductType.COMMODITY_SPOT, index.price());
            double quantity = componentAllocation / Math.max(1L, price);
            fund.buyProduct(componentId, FinancialProductType.COMMODITY_SPOT, quantity, price);
        }
    }

    FundTrackingMetrics metrics(FundState fund, MarketIndexState index, double creationRedemptionThreshold) {
        if (fund == null || index == null) {
            return new FundTrackingMetrics("", 0L, 0.0D, 0L, 0.0D, 0.0D, FundCreationRedemptionAction.NONE);
        }
        double navPerShare = fund.nav() / Math.max(1.0D, fund.totalFundShares());
        double trackingError = index.price() <= 0L ? 0.0D : (navPerShare - index.price()) / index.price();
        double premiumDiscount = navPerShare <= 0.0D ? 0.0D : (fund.sharePrice() - navPerShare) / navPerShare;
        FundCreationRedemptionAction action = action(premiumDiscount, creationRedemptionThreshold);
        return new FundTrackingMetrics(index.id(), index.price(), navPerShare, fund.sharePrice(), trackingError, premiumDiscount, action);
    }

    private static FundCreationRedemptionAction action(double premiumDiscount, double threshold) {
        double normalizedThreshold = Math.max(0.0D, threshold);
        if (premiumDiscount > normalizedThreshold) {
            return FundCreationRedemptionAction.CREATION;
        }
        if (premiumDiscount < -normalizedThreshold) {
            return FundCreationRedemptionAction.REDEMPTION;
        }
        return FundCreationRedemptionAction.NONE;
    }
}
