package com.fluorineuck.minance.product.component.collection;

import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.fluorineuck.minance.product.component.core.ComponentAttribute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentCollectionIndexTest {
    @Test
    void collectionContainsGenericComponentTypesOnly() {
        assertTrue(ComponentCollectionIndex.INSTANCE.contains(FinancialProductType.FUND));
        assertTrue(ComponentCollectionIndex.INSTANCE.contains(FinancialProductType.FUTURE));
        assertTrue(ComponentCollectionIndex.INSTANCE.contains(FinancialProductType.OPTION));
        assertTrue(ComponentCollectionIndex.INSTANCE.contains(FinancialProductType.STRUCTURED_PRODUCT));
        assertFalse(ComponentCollectionIndex.INSTANCE.contains(FinancialProductType.COMMODITY_SPOT));
    }

    @Test
    void entriesExposeDefaultOverlayAttributes() {
        ComponentCollectionEntry fund = ComponentCollectionIndex.INSTANCE.entry(FinancialProductType.FUND).orElseThrow();

        assertTrue(fund.defaultAttributes().contains(ComponentAttribute.NAV_ANCHOR));
        assertTrue(fund.defaultAttributes().contains(ComponentAttribute.TRACKING_TARGET));
    }
}
