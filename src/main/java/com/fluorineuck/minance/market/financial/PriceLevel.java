package com.fluorineuck.minance.market.financial;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.MarketConfig;
import net.minecraft.nbt.CompoundTag;

public final class PriceLevel {
    private final long priceTick;
    private double bidLiquidity;
    private double askLiquidity;
    private int bidOrderCount;
    private int askOrderCount;
    private long age;
    private int touchCount;
    private double bidConfidence;
    private double askConfidence;

    public PriceLevel(long priceTick) {
        this.priceTick = priceTick;
        double initialConfidence = ConfigRegistry.INSTANCE.market().financialMicrostructure().initialLevelConfidence();
        bidConfidence = initialConfidence;
        askConfidence = initialConfidence;
    }

    public long priceTick() { return priceTick; }
    public double bidLiquidity() { return bidLiquidity; }
    public double askLiquidity() { return askLiquidity; }
    public int bidOrderCount() { return bidOrderCount; }
    public int askOrderCount() { return askOrderCount; }
    public long age() { return age; }
    public int touchCount() { return touchCount; }
    public double bidConfidence() { return bidConfidence; }
    public double askConfidence() { return askConfidence; }

    public void addBid(double quantity, int orders) {
        MarketConfig.FinancialMicrostructure config = ConfigRegistry.INSTANCE.market().financialMicrostructure();
        bidLiquidity += Math.max(0.0D, quantity);
        bidOrderCount += Math.max(0, orders);
        bidConfidence = Math.min(config.maxLevelConfidence(), bidConfidence + Math.max(0, orders) * config.confidencePerOrder());
    }

    public void addAsk(double quantity, int orders) {
        MarketConfig.FinancialMicrostructure config = ConfigRegistry.INSTANCE.market().financialMicrostructure();
        askLiquidity += Math.max(0.0D, quantity);
        askOrderCount += Math.max(0, orders);
        askConfidence = Math.min(config.maxLevelConfidence(), askConfidence + Math.max(0, orders) * config.confidencePerOrder());
    }

    public void ageAndDecay(double ageDecay, double distanceDecay, double volatilityDecay, double maturityDecay) {
        age++;
        double decay = clamp(ageDecay * distanceDecay * volatilityDecay * maturityDecay, 0.0D, 1.0D);
        bidLiquidity *= decay;
        askLiquidity *= decay;
        double pruneLiquidity = ConfigRegistry.INSTANCE.market().financialMicrostructure().levelPruneLiquidity();
        if (bidLiquidity < pruneLiquidity) {
            bidLiquidity = 0.0D;
            bidOrderCount = 0;
        }
        if (askLiquidity < pruneLiquidity) {
            askLiquidity = 0.0D;
            askOrderCount = 0;
        }
    }

    public double consumeBid(double requested) {
        double consumed = Math.min(Math.max(0.0D, requested), bidLiquidity);
        bidLiquidity -= consumed;
        touchCount++;
        return consumed;
    }

    public double consumeAsk(double requested) {
        double consumed = Math.min(Math.max(0.0D, requested), askLiquidity);
        askLiquidity -= consumed;
        touchCount++;
        return consumed;
    }

    public double supportStrength(double ageWeight, double touchWeight) {
        return bidLiquidity * (1.0D + Math.log(1.0D + age) * ageWeight) * (1.0D + touchCount * touchWeight) * bidConfidence;
    }

    public double resistanceStrength(double ageWeight, double touchWeight) {
        return askLiquidity * (1.0D + Math.log(1.0D + age) * ageWeight) * (1.0D + touchCount * touchWeight) * askConfidence;
    }

    public boolean empty() {
        return bidLiquidity <= 0.0D && askLiquidity <= 0.0D;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("price_tick", priceTick);
        tag.putDouble("bid_liquidity", bidLiquidity);
        tag.putDouble("ask_liquidity", askLiquidity);
        tag.putInt("bid_order_count", bidOrderCount);
        tag.putInt("ask_order_count", askOrderCount);
        tag.putLong("age", age);
        tag.putInt("touch_count", touchCount);
        tag.putDouble("bid_confidence", bidConfidence);
        tag.putDouble("ask_confidence", askConfidence);
        return tag;
    }

    public static PriceLevel load(CompoundTag tag) {
        PriceLevel level = new PriceLevel(tag.getLong("price_tick"));
        level.bidLiquidity = tag.getDouble("bid_liquidity");
        level.askLiquidity = tag.getDouble("ask_liquidity");
        level.bidOrderCount = tag.getInt("bid_order_count");
        level.askOrderCount = tag.getInt("ask_order_count");
        level.age = tag.getLong("age");
        level.touchCount = tag.getInt("touch_count");
        level.bidConfidence = tag.getDouble("bid_confidence");
        level.askConfidence = tag.getDouble("ask_confidence");
        return level;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
