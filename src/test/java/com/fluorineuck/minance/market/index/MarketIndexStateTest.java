package com.fluorineuck.minance.market.index;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketIndexStateTest {
    @Test
    void savesAndLoadsCompositionAndCadenceTicks() {
        MarketIndexState state = new MarketIndexState("test_index", "Test Index", 100L);
        state.update(120L, 2, List.of("minecraft:wheat", "minecraft:bread"), 10, 24000L, 24400L);

        CompoundTag tag = state.save();
        MarketIndexState loaded = MarketIndexState.load(tag);

        assertEquals(List.of("minecraft:wheat", "minecraft:bread"), loaded.componentIds());
        assertEquals(24000L, loaded.lastReconstitutionTick());
        assertEquals(24400L, loaded.lastRebalanceTick());
        assertEquals(120L, loaded.price());
        assertEquals(2, loaded.componentCount());
    }
}
