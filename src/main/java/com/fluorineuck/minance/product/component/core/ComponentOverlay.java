package com.fluorineuck.minance.product.component.core;

import com.fluorineuck.minance.market.financial.FinancialProductType;

public record ComponentOverlay(
        String productId,
        FinancialProductType productType,
        ComponentAttributeSet attributes
) {
    public ComponentOverlay {
        productId = productId == null ? "" : productId;
        productType = productType == null ? FinancialProductType.STRUCTURED_PRODUCT : productType;
        attributes = attributes == null ? ComponentAttributeSet.empty() : attributes;
    }

    public boolean has(ComponentAttribute attribute) {
        return attributes.contains(attribute);
    }
}
