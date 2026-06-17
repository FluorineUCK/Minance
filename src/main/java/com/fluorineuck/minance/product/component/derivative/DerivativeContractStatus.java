package com.fluorineuck.minance.product.derivative;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DerivativeContractStatus implements StringRepresentable {
    OPEN("open"),
    CLOSED("closed"),
    SETTLED("settled");

    public static final Codec<DerivativeContractStatus> CODEC = StringRepresentable.fromEnum(DerivativeContractStatus::values);

    private final String serializedName;

    DerivativeContractStatus(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static DerivativeContractStatus byName(String name) {
        for (DerivativeContractStatus status : values()) {
            if (status.serializedName.equals(name)) {
                return status;
            }
        }
        return OPEN;
    }
}
