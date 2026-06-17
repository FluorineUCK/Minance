package com.fluorineuck.minance.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CommodityConfig(
        double initialTargetInventory,
        double defaultTargetInventory,
        double defaultVolatility,
        double referenceObservedWeight
) {
    public static final Codec<CommodityConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("initial_target_inventory").forGetter(CommodityConfig::initialTargetInventory),
            Codec.DOUBLE.fieldOf("default_target_inventory").forGetter(CommodityConfig::defaultTargetInventory),
            Codec.DOUBLE.fieldOf("default_volatility").forGetter(CommodityConfig::defaultVolatility),
            Codec.DOUBLE.fieldOf("reference_observed_weight").forGetter(CommodityConfig::referenceObservedWeight)
    ).apply(instance, CommodityConfig::new));

    public static CommodityConfig defaults() {
        return new CommodityConfig(64.0D, 256.0D, 0.05D, 0.02D);
    }
}
