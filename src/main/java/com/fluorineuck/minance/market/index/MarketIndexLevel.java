package com.fluorineuck.minance.market.index;

public record MarketIndexLevel(
        long price,
        int componentCount,
        double totalWeight
) {
    public MarketIndexLevel {
        price = Math.max(1L, price);
        componentCount = Math.max(0, componentCount);
        totalWeight = Math.max(0.0D, totalWeight);
    }
}
