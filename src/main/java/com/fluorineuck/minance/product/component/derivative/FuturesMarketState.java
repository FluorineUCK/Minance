package com.fluorineuck.minance.product.component.derivative;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.entity.company.MarketPriceBar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;

public final class FuturesMarketState {
    private final String id;
    private final String underlyingProductId;
    private final int depth;
    private final int durationDays;
    private long price;
    private int buyVolume;
    private int sellVolume;
    private final Deque<Long> priceHistory = new ArrayDeque<>();
    private final Deque<MarketPriceBar> priceBars = new ArrayDeque<>();

    public FuturesMarketState(String id, String underlyingProductId, int depth, int durationDays, long price) {
        this.id = id;
        this.underlyingProductId = underlyingProductId == null || underlyingProductId.isBlank() ? "minecraft:air" : underlyingProductId;
        this.depth = Math.max(0, depth);
        this.durationDays = Math.max(0, durationDays);
        this.price = Math.max(1L, price);
        rememberPrice(this.price, ConfigRegistry.INSTANCE.market().priceHistoryLimit());
    }

    public String id() {
        return id;
    }

    public String underlyingProductId() {
        return underlyingProductId;
    }

    public int depth() {
        return depth;
    }

    public ResourceLocation commodity() {
        ResourceLocation parsed = ResourceLocation.tryParse(underlyingProductId);
        return parsed == null ? ResourceLocation.withDefaultNamespace("air") : parsed;
    }

    public int durationDays() {
        return durationDays;
    }

    public long price() {
        return price;
    }

    public void setPrice(long price, int historyLimit) {
        this.price = Math.max(1L, price);
        rememberPrice(this.price, historyLimit);
    }

    public int buyVolume() {
        return buyVolume;
    }

    public int sellVolume() {
        return sellVolume;
    }

    public void addVolume(int buy, int sell) {
        buyVolume += Math.max(0, buy);
        sellVolume += Math.max(0, sell);
    }

    public void resetVolume() {
        buyVolume = 0;
        sellVolume = 0;
    }

    public Deque<Long> priceHistory() {
        return new ArrayDeque<>(priceHistory);
    }

    public Deque<MarketPriceBar> priceBars() {
        return new ArrayDeque<>(priceBars);
    }

    public void appendBar(long startTick, long endTick, long openPrice, long highPrice, long lowPrice, long closePrice, int historyLimit) {
        long high = Math.max(Math.max(openPrice, closePrice), highPrice);
        long low = Math.max(1L, Math.min(Math.min(openPrice, closePrice), lowPrice));
        priceBars.addLast(new MarketPriceBar(startTick, endTick, Math.max(1L, openPrice), high, low, Math.max(1L, closePrice), buyVolume, sellVolume));
        while (priceBars.size() > Math.max(1, historyLimit)) {
            priceBars.removeFirst();
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("underlying_product", underlyingProductId);
        tag.putInt("depth", depth);
        tag.putInt("duration_days", durationDays);
        tag.putLong("price", price);
        tag.putInt("buy_volume", buyVolume);
        tag.putInt("sell_volume", sellVolume);
        ListTag history = new ListTag();
        priceHistory.forEach(value -> history.add(LongTag.valueOf(value)));
        tag.put("history", history);
        ListTag bars = new ListTag();
        priceBars.forEach(bar -> bars.add(bar.save()));
        tag.put("bars", bars);
        return tag;
    }

    public static FuturesMarketState load(CompoundTag tag) {
        String underlying = tag.contains("underlying_product", Tag.TAG_STRING) ? tag.getString("underlying_product") : tag.getString("commodity");
        FuturesMarketState state = new FuturesMarketState(
                tag.getString("id"),
                underlying,
                tag.getInt("depth"),
                tag.getInt("duration_days"),
                tag.getLong("price")
        );
        state.buyVolume = tag.getInt("buy_volume");
        state.sellVolume = tag.getInt("sell_volume");
        state.priceHistory.clear();
        ListTag history = tag.getList("history", Tag.TAG_LONG);
        for (int i = 0; i < history.size(); i++) {
            Tag value = history.get(i);
            if (value instanceof NumericTag numeric) {
                state.priceHistory.addLast(numeric.getAsLong());
            }
        }
        if (state.priceHistory.isEmpty()) {
            state.rememberPrice(state.price, ConfigRegistry.INSTANCE.market().priceHistoryLimit());
        }
        state.priceBars.clear();
        ListTag bars = tag.getList("bars", Tag.TAG_COMPOUND);
        for (int i = 0; i < bars.size(); i++) {
            state.priceBars.addLast(MarketPriceBar.load(bars.getCompound(i)));
        }
        return state;
    }

    private void rememberPrice(long value, int limit) {
        priceHistory.addLast(value);
        while (priceHistory.size() > Math.max(1, limit)) {
            priceHistory.removeFirst();
        }
    }
}
