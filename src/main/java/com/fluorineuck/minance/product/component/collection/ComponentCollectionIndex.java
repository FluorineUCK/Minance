package com.fluorineuck.minance.product.component.collection;

import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.product.component.GenericProductComponent;
import com.fluorineuck.minance.product.component.core.ComponentOverlayResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class ComponentCollectionIndex {
    public static final ComponentCollectionIndex INSTANCE = new ComponentCollectionIndex();

    private final List<ComponentCollectionEntry> entries = Arrays.stream(GenericProductComponent.values())
            .map(component -> new ComponentCollectionEntry(
                    component,
                    component.financialProductType(),
                    component.ownerPackage(),
                    ComponentOverlayResolver.INSTANCE.defaultAttributes(component.financialProductType())
            ))
            .toList();

    private ComponentCollectionIndex() {
    }

    public List<ComponentCollectionEntry> entries() {
        return entries;
    }

    public boolean contains(FinancialProductType productType) {
        return entry(productType).isPresent();
    }

    public Optional<ComponentCollectionEntry> entry(FinancialProductType productType) {
        return entries.stream()
                .filter(entry -> entry.productType() == productType)
                .findFirst();
    }
}
