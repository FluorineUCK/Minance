package com.fluorineuck.minance.product.equity;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum EquityAssetType implements StringRepresentable {
    COMMON_STOCK("common_stock"),
    VILLAGER_FOUNDER_STOCK("villager_founder_stock"),
    TREASURY_STOCK("treasury_stock"),
    PREFERRED_STOCK("preferred_stock");

    public static final Codec<EquityAssetType> CODEC = StringRepresentable.fromEnum(EquityAssetType::values);

    private final String serializedName;

    EquityAssetType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
