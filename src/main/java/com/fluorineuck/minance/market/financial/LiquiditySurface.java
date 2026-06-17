package com.fluorineuck.minance.market.financial;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class LiquiditySurface {
    private final NavigableMap<Long, PriceLevel> levels = new TreeMap<>();

    public NavigableMap<Long, PriceLevel> levels() {
        return levels;
    }

    public PriceLevel level(long priceTick) {
        return levels.computeIfAbsent(priceTick, PriceLevel::new);
    }

    public void addBid(long priceTick, double liquidity, int orderCount, MarketActivityStats stats) {
        level(priceTick).addBid(liquidity, orderCount);
        stats.recordGeneratedBuy(liquidity, orderCount);
    }

    public void addAsk(long priceTick, double liquidity, int orderCount, MarketActivityStats stats) {
        level(priceTick).addAsk(liquidity, orderCount);
        stats.recordGeneratedSell(liquidity, orderCount);
    }

    public void decay(long currentTick, double tickSize, double realizedVolatility, int maturityDays, double ageLambda, double distanceLambda, double volatilityLambda, double maturityLambda) {
        double volatilityDecay = Math.exp(-volatilityLambda * Math.max(0.0D, realizedVolatility));
        double maturityDecay = Math.exp(-maturityLambda / Math.max(1.0D, maturityDays + 1.0D));
        levels.values().forEach(level -> {
            double ageDecay = Math.exp(-ageLambda * Math.max(0L, level.age()));
            double distance = Math.abs(level.priceTick() - currentTick) * Math.max(1.0D, tickSize);
            double distanceDecay = Math.exp(-distanceLambda * distance);
            level.ageAndDecay(ageDecay, distanceDecay, volatilityDecay, maturityDecay);
        });
        levels.entrySet().removeIf(entry -> entry.getValue().empty());
    }

    public double nearBidLiquidity(long currentTick, int radius) {
        return levels.subMap(currentTick - radius, true, currentTick, true).values().stream().mapToDouble(PriceLevel::bidLiquidity).sum();
    }

    public double nearAskLiquidity(long currentTick, int radius) {
        return levels.subMap(currentTick, true, currentTick + radius, true).values().stream().mapToDouble(PriceLevel::askLiquidity).sum();
    }

    public PriceLevel strongestSupport(double ageWeight, double touchWeight) {
        return levels.values().stream().max(Comparator.comparingDouble(level -> level.supportStrength(ageWeight, touchWeight))).orElse(null);
    }

    public PriceLevel strongestResistance(double ageWeight, double touchWeight) {
        return levels.values().stream().max(Comparator.comparingDouble(level -> level.resistanceStrength(ageWeight, touchWeight))).orElse(null);
    }

    public void consumeBetween(long fromTick, long toTick, double volume, MarketActivityStats stats) {
        if (fromTick == toTick || volume <= 0.0D) {
            return;
        }
        long low = Math.min(fromTick, toTick);
        long high = Math.max(fromTick, toTick);
        for (PriceLevel level : levels.subMap(low, true, high, true).values()) {
            if (toTick > fromTick) {
                double consumed = level.consumeAsk(volume);
                if (consumed > 0.0D) {
                    stats.recordConsumedSell(consumed);
                }
            } else {
                double consumed = level.consumeBid(volume);
                if (consumed > 0.0D) {
                    stats.recordConsumedBuy(consumed);
                }
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        levels.values().forEach(level -> list.add(level.save()));
        tag.put("levels", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        levels.clear();
        ListTag list = tag.getList("levels", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            PriceLevel level = PriceLevel.load(list.getCompound(i));
            levels.put(level.priceTick(), level);
        }
    }
}
