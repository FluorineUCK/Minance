package com.fluorineuck.minance.product.component.fund;

public record FundTrackingMetrics(
        String indexId,
        long indexLevel,
        double navPerShare,
        long marketPrice,
        double trackingError,
        double premiumDiscount,
        FundCreationRedemptionAction creationRedemptionAction
) {
    public FundTrackingMetrics {
        indexId = indexId == null ? "" : indexId;
        indexLevel = Math.max(0L, indexLevel);
        navPerShare = Math.max(0.0D, navPerShare);
        marketPrice = Math.max(0L, marketPrice);
        creationRedemptionAction = creationRedemptionAction == null ? FundCreationRedemptionAction.NONE : creationRedemptionAction;
    }
}
