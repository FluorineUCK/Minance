package com.fluorineuck.minance.entity.institution;

import com.fluorineuck.minance.market.financial.FinancialProductType;

public record InstitutionSignalRequest(
        String productId,
        FinancialProductType productType,
        long horizonTicks,
        long anchorPrice,
        double clientFlowStrength,
        double riskDiscountStrength,
        double liquidityBid,
        double liquidityAsk
) {
    public InstitutionSignalRequest {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        productType = productType == null ? FinancialProductType.STRUCTURED_PRODUCT : productType;
        horizonTicks = Math.max(0L, horizonTicks);
        anchorPrice = Math.max(0L, anchorPrice);
        riskDiscountStrength = Math.max(0.0D, riskDiscountStrength);
        liquidityBid = Math.max(0.0D, liquidityBid);
        liquidityAsk = Math.max(0.0D, liquidityAsk);
    }
}
