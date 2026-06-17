package com.fluorineuck.minance.product.derivative;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum OptionRight implements StringRepresentable {
    CALL("call"),
    PUT("put");

    public static final Codec<OptionRight> CODEC = StringRepresentable.fromEnum(OptionRight::values);

    private final String serializedName;

    OptionRight(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static OptionRight byName(String name) {
        return "put".equals(name) ? PUT : CALL;
    }
}
