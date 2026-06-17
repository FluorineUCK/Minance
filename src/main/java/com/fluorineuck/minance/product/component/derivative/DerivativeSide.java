package com.fluorineuck.minance.product.derivative;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DerivativeSide implements StringRepresentable {
    BUY("buy"),
    SELL("sell");

    public static final Codec<DerivativeSide> CODEC = StringRepresentable.fromEnum(DerivativeSide::values);

    private final String serializedName;

    DerivativeSide(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static DerivativeSide byName(String name) {
        return "sell".equals(name) ? SELL : BUY;
    }
}
