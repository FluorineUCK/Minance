package com.fluorineuck.minance.market.index;

import com.fluorineuck.minance.config.ConfigRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayDeque;
import java.util.Deque;

public final class MarketIndexState {
    private final String id;
    private final String name;
    private long price;
    private long previousPrice;
    private int componentCount;
    private final Deque<Long> history = new ArrayDeque<>();

    public MarketIndexState(String id, String name, long price) {
        this.id = id;
        this.name = name;
        this.price = Math.max(1L, price);
        this.previousPrice = this.price;
        remember(this.price, ConfigRegistry.INSTANCE.market().priceHistoryLimit());
    }

    public String id() { return id; }
    public String name() { return name; }
    public long price() { return price; }
    public long previousPrice() { return previousPrice; }
    public long delta() { return price - previousPrice; }
    public int componentCount() { return componentCount; }
    public Deque<Long> history() { return new ArrayDeque<>(history); }

    public void update(long nextPrice, int componentCount, int historyLimit) {
        this.previousPrice = this.price;
        this.price = Math.max(1L, nextPrice);
        this.componentCount = Math.max(0, componentCount);
        remember(this.price, historyLimit);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("name", name);
        tag.putLong("price", price);
        tag.putLong("previous_price", previousPrice);
        tag.putInt("component_count", componentCount);
        ListTag historyTag = new ListTag();
        history.forEach(value -> historyTag.add(LongTag.valueOf(value)));
        tag.put("history", historyTag);
        return tag;
    }

    public static MarketIndexState load(CompoundTag tag) {
        MarketIndexState state = new MarketIndexState(tag.getString("id"), tag.getString("name"), tag.getLong("price"));
        state.previousPrice = tag.getLong("previous_price");
        state.componentCount = tag.getInt("component_count");
        state.history.clear();
        ListTag historyTag = tag.getList("history", Tag.TAG_LONG);
        for (int i = 0; i < historyTag.size(); i++) {
            Tag value = historyTag.get(i);
            if (value instanceof NumericTag numeric) {
                state.history.addLast(numeric.getAsLong());
            }
        }
        if (state.history.isEmpty()) {
            state.remember(state.price, ConfigRegistry.INSTANCE.market().priceHistoryLimit());
        }
        return state;
    }

    private void remember(long value, int historyLimit) {
        history.addLast(value);
        while (history.size() > Math.max(1, historyLimit)) {
            history.removeFirst();
        }
    }
}
