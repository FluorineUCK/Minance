package com.fluorineuck.minance.product.commodity.spot;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum SpotMarketSourceType implements StringRepresentable {
    VILLAGER_BUY_ORDER("villager_buy_order"),
    VILLAGER_SELL_ORDER("villager_sell_order"),
    VILLAGER_PRODUCTION("villager_production"),
    PLAYER_ORDER("player_order"),
    COMPANY_ORDER("company_order"),
    EVENT("event"),
    IMPORT("import"),
    EXPORT("export");

    private final String serializedName;

    SpotMarketSourceType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static SpotMarketSourceType byName(String name) {
        String normalized = name == null ? "" : name.toLowerCase(Locale.ROOT);
        for (SpotMarketSourceType type : values()) {
            if (type.serializedName.equals(normalized) || type.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return type;
            }
        }
        return EVENT;
    }
}
