package com.fluorineuck.minance.product.component.derivative;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DerivativeDeliveryMethod implements StringRepresentable {
    CASH_SETTLEMENT("cash_settlement"),
    PHYSICAL_DELIVERY("physical_delivery"),
    AUTO_CLOSE("auto_close");

    public static final Codec<DerivativeDeliveryMethod> CODEC = StringRepresentable.fromEnum(DerivativeDeliveryMethod::values);

    private final String serializedName;

    DerivativeDeliveryMethod(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static DerivativeDeliveryMethod byName(String name) {
        for (DerivativeDeliveryMethod method : values()) {
            if (method.serializedName.equals(name)) {
                return method;
            }
        }
        return CASH_SETTLEMENT;
    }
}
