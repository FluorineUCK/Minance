package com.fluorineuck.minance.entity.company;

import net.minecraft.nbt.CompoundTag;

public record MarketPriceBar(
        long startTick,
        long endTick,
        long open,
        long high,
        long low,
        long close,
        int buyVolume,
        int sellVolume
) {
    public int volume() {
        return Math.max(0, buyVolume) + Math.max(0, sellVolume);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("start_tick", startTick);
        tag.putLong("end_tick", endTick);
        tag.putLong("open", open);
        tag.putLong("high", high);
        tag.putLong("low", low);
        tag.putLong("close", close);
        tag.putInt("buy_volume", buyVolume);
        tag.putInt("sell_volume", sellVolume);
        return tag;
    }

    public static MarketPriceBar load(CompoundTag tag) {
        return new MarketPriceBar(
                tag.getLong("start_tick"),
                tag.getLong("end_tick"),
                Math.max(1L, tag.getLong("open")),
                Math.max(1L, tag.getLong("high")),
                Math.max(1L, tag.getLong("low")),
                Math.max(1L, tag.getLong("close")),
                Math.max(0, tag.getInt("buy_volume")),
                Math.max(0, tag.getInt("sell_volume"))
        );
    }
}
