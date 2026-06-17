package com.fluorineuck.minance.product.commodity.spot;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record SpotMarketRow(
        ResourceLocation item,
        long price,
        long previousPrice,
        int volume,
        int inventory,
        double volatility,
        Map<String, Double> supplyBreakdown,
        Map<String, Double> demandBreakdown
) {
    public long priceDelta() {
        return price - previousPrice;
    }
}
