package com.fluorineuck.minance.product.component.core;

import com.fluorineuck.minance.market.financial.FinancialProductType;

public final class ComponentOverlayResolver {
    public static final ComponentOverlayResolver INSTANCE = new ComponentOverlayResolver();

    private ComponentOverlayResolver() {
    }

    public ComponentOverlay resolve(String productId, FinancialProductType productType) {
        return new ComponentOverlay(productId, productType, defaultAttributes(productType));
    }

    public ComponentAttributeSet defaultAttributes(FinancialProductType productType) {
        return switch (productType == null ? FinancialProductType.STRUCTURED_PRODUCT : productType) {
            case FUTURE -> ComponentAttributeSet.of(
                    ComponentAttribute.UNDERLYING_EXPOSURE,
                    ComponentAttribute.MATURITY,
                    ComponentAttribute.SETTLEMENT
            );
            case OPTION -> ComponentAttributeSet.of(
                    ComponentAttribute.UNDERLYING_EXPOSURE,
                    ComponentAttribute.MATURITY,
                    ComponentAttribute.PAYOFF_SHAPE,
                    ComponentAttribute.LEVERAGE
            );
            case FUND -> ComponentAttributeSet.of(
                    ComponentAttribute.BASKET_EXPOSURE,
                    ComponentAttribute.NAV_ANCHOR,
                    ComponentAttribute.LIQUIDITY_WRAPPER,
                    ComponentAttribute.TRACKING_TARGET
            );
            case STRUCTURED_PRODUCT -> ComponentAttributeSet.of(
                    ComponentAttribute.BASKET_EXPOSURE,
                    ComponentAttribute.PAYOFF_SHAPE,
                    ComponentAttribute.CLAIM_PRIORITY
            );
            case EQUITY, BOND, COMMODITY_SPOT -> ComponentAttributeSet.empty();
        };
    }
}
