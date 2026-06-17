package com.fluorineuck.minance.market.financial;

public record FinancialMarketResult(
        String productId,
        FinancialProductType productType,
        long previousPrice,
        long nextPrice,
        long highPrice,
        long lowPrice,
        int buyVolume,
        int sellVolume,
        double imbalance,
        double nearestBidLiquidity,
        double nearestAskLiquidity,
        long strongestSupportPrice,
        long strongestResistancePrice,
        MarketActivityStats stats
) {
}
