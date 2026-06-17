package com.fluorineuck.minance.market.financial;

import java.util.Objects;

public record FundamentalAnchor(
        String productId,
        FinancialProductType productType,
        FundamentalAnchorType anchorType,
        PriceSignalSource source,
        long anchorPrice,
        double confidence,
        long horizonTicks
) {
    public FundamentalAnchor {
        productId = requireProductId(productId);
        productType = Objects.requireNonNull(productType, "productType");
        anchorType = anchorType == null ? FundamentalAnchorType.UNKNOWN : anchorType;
        source = source == null ? PriceSignalSource.UNKNOWN : source;
        anchorPrice = Math.max(0L, anchorPrice);
        confidence = clamp(confidence, 0.0D, 1.0D);
        horizonTicks = Math.max(0L, horizonTicks);
    }

    public boolean active() {
        return anchorPrice > 0L && confidence > 0.0D;
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
