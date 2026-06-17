package com.fluorineuck.minance.product.commodity.spot;

import com.fluorineuck.minance.config.CommodityConfig;
import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.market.financial.MarketFlowSnapshot;
import com.fluorineuck.minance.product.commodity.core.CommodityState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayDeque;
import java.util.Deque;

public final class SpotMarketEngine {
    private final CommodityState commodity;
    private double currentPrice;
    private double nextPrice;
    private double priceElasticity;
    private double adjustmentSpeed;
    private double inventoryWeight;
    private double flowWeight;
    private double stabilizerWeight;
    private final Deque<Long> priceHistory = new ArrayDeque<>();

    public SpotMarketEngine(CommodityState commodity, double currentPrice) {
        this.commodity = commodity;
        this.currentPrice = Math.max(1.0D, currentPrice);
        this.nextPrice = this.currentPrice;
        this.priceElasticity = 1.0D;
        rememberPrice(Math.round(this.currentPrice), ConfigRegistry.INSTANCE.market().priceHistoryLimit());
    }

    public CommodityState commodity() {
        return commodity;
    }

    public long price() {
        return Math.max(1L, Math.round(currentPrice));
    }

    public double currentPrice() {
        return currentPrice;
    }

    public double nextPrice() {
        return nextPrice;
    }

    public double priceElasticity() {
        return priceElasticity;
    }

    public void setPriceElasticity(double priceElasticity) {
        this.priceElasticity = Math.max(0.0D, priceElasticity);
    }

    public Deque<Long> priceHistory() {
        return new ArrayDeque<>(priceHistory);
    }

    public long previousPrice() {
        if (priceHistory.size() < 2) {
            return price();
        }
        Long previous = null;
        Long current = null;
        for (Long value : priceHistory) {
            previous = current;
            current = value;
        }
        return previous == null ? price() : previous;
    }

    public void configure(MarketConfig marketConfig, CommodityConfig commodityConfig) {
        MarketConfig.SpotPricing spot = marketConfig.spot();
        commodity.setTargetInventory(commodityConfig.defaultTargetInventory());
        adjustmentSpeed = Math.max(0.0D, spot.adjustmentSpeed());
        inventoryWeight = spot.inventoryWeight();
        flowWeight = spot.flowWeight();
        stabilizerWeight = spot.stabilizerWeight();
    }

    public void settle(MarketFlowSnapshot flowSnapshot, MarketConfig marketConfig, CommodityConfig commodityConfig) {
        configure(marketConfig, commodityConfig);
        MarketConfig.SpotPricing spot = marketConfig.spot();
        double inventoryRatio = commodity.inventory() / Math.max(commodity.targetInventory(), 1.0D);
        double flowPressure = commodity.outflow() - commodity.inflow();
        double stabilizerPressure = flowSnapshot == null ? 0.0D : flowSnapshot.stabilizerFlow();
        double targetPrice = commodity.referencePrice()
                * (1.0D + inventoryWeight * (1.0D - inventoryRatio))
                * (1.0D + flowWeight * flowPressure)
                * (1.0D + stabilizerWeight * stabilizerPressure);
        double current = currentPrice;
        double next = current + (targetPrice - current) * adjustmentSpeed;
        double maxChange = Math.max(0.0D, spot.maxCycleChange());
        next = clamp(next, current * (1.0D - maxChange), current * (1.0D + maxChange));
        next = clamp(next, spot.minPrice(), spot.maxPrice());
        applyNextPrice(next, marketConfig.priceHistoryLimit());
    }

    public void applyNextPrice(double nextPrice, int historyLimit) {
        this.nextPrice = Math.max(1.0D, nextPrice);
        this.currentPrice = this.nextPrice;
        rememberPrice(Math.round(this.currentPrice), historyLimit);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("current_price", currentPrice);
        tag.putDouble("next_price", nextPrice);
        tag.putDouble("price_elasticity", priceElasticity);
        tag.putDouble("adjustment_speed", adjustmentSpeed);
        tag.putDouble("inventory_weight", inventoryWeight);
        tag.putDouble("flow_weight", flowWeight);
        tag.putDouble("stabilizer_weight", stabilizerWeight);
        ListTag history = new ListTag();
        priceHistory.forEach(value -> history.add(LongTag.valueOf(value)));
        tag.put("price_history", history);
        return tag;
    }

    public void load(CompoundTag tag) {
        currentPrice = tag.contains("current_price", Tag.TAG_DOUBLE) ? tag.getDouble("current_price") : tag.getLong("price");
        nextPrice = tag.contains("next_price", Tag.TAG_DOUBLE) ? tag.getDouble("next_price") : currentPrice;
        priceElasticity = tag.contains("price_elasticity", Tag.TAG_DOUBLE) ? tag.getDouble("price_elasticity") : 1.0D;
        adjustmentSpeed = tag.getDouble("adjustment_speed");
        inventoryWeight = tag.getDouble("inventory_weight");
        flowWeight = tag.getDouble("flow_weight");
        stabilizerWeight = tag.getDouble("stabilizer_weight");
        priceHistory.clear();
        ListTag history = tag.getList("price_history", Tag.TAG_LONG);
        for (int i = 0; i < history.size(); i++) {
            Tag value = history.get(i);
            if (value instanceof NumericTag numeric) {
                priceHistory.addLast(numeric.getAsLong());
            }
        }
        if (priceHistory.isEmpty()) {
            rememberPrice(Math.round(currentPrice), ConfigRegistry.INSTANCE.market().priceHistoryLimit());
        }
    }

    private void rememberPrice(long value, int limit) {
        priceHistory.addLast(Math.max(1L, value));
        while (priceHistory.size() > Math.max(1, limit)) {
            priceHistory.removeFirst();
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
