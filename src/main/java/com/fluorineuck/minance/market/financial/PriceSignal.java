package com.fluorineuck.minance.market.financial;

import java.util.Objects;

public record PriceSignal(
        String productId,
        FinancialProductType productType,
        PriceSignalSource source,
        PriceSignalDirection direction,
        double strength,
        double confidence,
        long horizonTicks,
        long anchorPrice,
        double liquidityBid,
        double liquidityAsk
) {
    public PriceSignal {
        productId = requireProductId(productId);
        productType = Objects.requireNonNull(productType, "productType");
        source = source == null ? PriceSignalSource.UNKNOWN : source;
        direction = direction == null ? PriceSignalDirection.NEUTRAL : direction;
        strength = Math.max(0.0D, strength);
        confidence = clamp(confidence, 0.0D, 1.0D);
        horizonTicks = Math.max(0L, horizonTicks);
        anchorPrice = Math.max(0L, anchorPrice);
        liquidityBid = Math.max(0.0D, liquidityBid);
        liquidityAsk = Math.max(0.0D, liquidityAsk);
    }

    public double signedStrength() {
        return direction.signedStrength(strength) * confidence;
    }

    public boolean hasAnchor() {
        return anchorPrice > 0L;
    }

    public boolean hasLiquidity() {
        return liquidityBid > 0.0D || liquidityAsk > 0.0D;
    }

    private static String requireProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        return productId;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
