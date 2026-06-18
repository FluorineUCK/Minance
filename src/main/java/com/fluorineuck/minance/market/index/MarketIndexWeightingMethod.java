package com.fluorineuck.minance.market.index;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MarketIndexWeightingMethod implements StringRepresentable {
    EQUAL_WEIGHTED("equal_weighted"),
    VOLUME_WEIGHTED("volume_weighted"),
    ORDER_COUNT_WEIGHTED("order_count_weighted");

    public static final Codec<MarketIndexWeightingMethod> CODEC = StringRepresentable.fromEnum(MarketIndexWeightingMethod::values);

    private final String serializedName;

    MarketIndexWeightingMethod(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
