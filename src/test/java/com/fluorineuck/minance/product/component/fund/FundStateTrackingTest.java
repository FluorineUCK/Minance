package com.fluorineuck.minance.product.fund;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FundStateTrackingTest {
    @Test
    void savesAndLoadsTrackingMetrics() {
        FundState fund = new FundState("fund:test", "Fund Test", "manager", "tracking:test_index", 1_000.0D, 10.0D);
        fund.recordTrackingMetrics(new FundTrackingMetrics("test_index", 100L, 100.0D, 120L, 0.0D, 0.2D, FundCreationRedemptionAction.CREATION));

        CompoundTag tag = fund.save();
        FundState loaded = FundState.load(tag);

        assertEquals("test_index", loaded.trackingIndexId());
        assertEquals(0.2D, loaded.premiumDiscount());
        assertEquals(FundCreationRedemptionAction.CREATION, loaded.creationRedemptionAction());
    }
}
