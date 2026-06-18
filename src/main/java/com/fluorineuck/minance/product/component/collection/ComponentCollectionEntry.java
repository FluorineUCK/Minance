package com.fluorineuck.minance.product.component.collection;

import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.product.component.GenericProductComponent;
import com.fluorineuck.minance.product.component.core.ComponentAttributeSet;

public record ComponentCollectionEntry(
        GenericProductComponent component,
        FinancialProductType productType,
        String ownerPackage,
        ComponentAttributeSet defaultAttributes
) {
    public ComponentCollectionEntry {
        if (component == null) {
            throw new IllegalArgumentException("component must not be null");
        }
        productType = productType == null ? component.financialProductType() : productType;
        ownerPackage = ownerPackage == null || ownerPackage.isBlank() ? component.ownerPackage() : ownerPackage;
        defaultAttributes = defaultAttributes == null ? ComponentAttributeSet.empty() : defaultAttributes;
    }
}
