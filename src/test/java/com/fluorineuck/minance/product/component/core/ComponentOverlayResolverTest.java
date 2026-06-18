package com.fluorineuck.minance.product.component.core;

import com.fluorineuck.minance.market.financial.FinancialProductType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentOverlayResolverTest {
    @Test
    void fundOverlayCarriesNavAndTrackingAttributes() {
        ComponentOverlay overlay = ComponentOverlayResolver.INSTANCE.resolve("fund:test", FinancialProductType.FUND);

        assertTrue(overlay.has(ComponentAttribute.BASKET_EXPOSURE));
        assertTrue(overlay.has(ComponentAttribute.NAV_ANCHOR));
        assertTrue(overlay.has(ComponentAttribute.TRACKING_TARGET));
        assertFalse(overlay.has(ComponentAttribute.PAYOFF_SHAPE));
    }

    @Test
    void commoditySpotIsNotAGenericComponentOverlay() {
        ComponentOverlay overlay = ComponentOverlayResolver.INSTANCE.resolve("minecraft:wheat", FinancialProductType.COMMODITY_SPOT);

        assertTrue(overlay.attributes().attributes().isEmpty());
    }
}
