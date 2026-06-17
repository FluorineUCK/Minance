package com.fluorineuck.minance.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EconomyConfig(
        long emeraldFundValue,
        long defaultItemPrice,
        int fallbackExpectedShares,
        double fallbackProxyLiquidityDemand
) {
    public static final Codec<EconomyConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("emerald_fund_value").forGetter(EconomyConfig::emeraldFundValue),
            Codec.LONG.fieldOf("default_item_price").forGetter(EconomyConfig::defaultItemPrice),
            Codec.INT.fieldOf("fallback_expected_shares").forGetter(EconomyConfig::fallbackExpectedShares),
            Codec.DOUBLE.fieldOf("fallback_proxy_liquidity_demand").forGetter(EconomyConfig::fallbackProxyLiquidityDemand)
    ).apply(instance, EconomyConfig::new));

    public static EconomyConfig defaults() {
        return new EconomyConfig(100L, 25L, 1, 1.0D);
    }
}
