package com.fluorineuck.minance.market.financial;

import net.minecraft.nbt.CompoundTag;

public final class MarketActivityStats {
    private int generatedOrderCount;
    private int generatedBuyOrderCount;
    private int generatedSellOrderCount;
    private int matchedTradeCount;
    private double generatedBuyLiquidity;
    private double generatedSellLiquidity;
    private double consumedBuyLiquidity;
    private double consumedSellLiquidity;

    public int generatedOrderCount() { return generatedOrderCount; }
    public int generatedBuyOrderCount() { return generatedBuyOrderCount; }
    public int generatedSellOrderCount() { return generatedSellOrderCount; }
    public int matchedTradeCount() { return matchedTradeCount; }
    public double generatedBuyLiquidity() { return generatedBuyLiquidity; }
    public double generatedSellLiquidity() { return generatedSellLiquidity; }
    public double consumedBuyLiquidity() { return consumedBuyLiquidity; }
    public double consumedSellLiquidity() { return consumedSellLiquidity; }

    public void recordGeneratedBuy(double liquidity, int orders) {
        int count = Math.max(0, orders);
        generatedOrderCount += count;
        generatedBuyOrderCount += count;
        generatedBuyLiquidity += Math.max(0.0D, liquidity);
    }

    public void recordGeneratedSell(double liquidity, int orders) {
        int count = Math.max(0, orders);
        generatedOrderCount += count;
        generatedSellOrderCount += count;
        generatedSellLiquidity += Math.max(0.0D, liquidity);
    }

    public void recordConsumedBuy(double liquidity) {
        consumedBuyLiquidity += Math.max(0.0D, liquidity);
        matchedTradeCount++;
    }

    public void recordConsumedSell(double liquidity) {
        consumedSellLiquidity += Math.max(0.0D, liquidity);
        matchedTradeCount++;
    }

    public void resetCycle() {
        generatedOrderCount = 0;
        generatedBuyOrderCount = 0;
        generatedSellOrderCount = 0;
        matchedTradeCount = 0;
        generatedBuyLiquidity = 0.0D;
        generatedSellLiquidity = 0.0D;
        consumedBuyLiquidity = 0.0D;
        consumedSellLiquidity = 0.0D;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("generated_order_count", generatedOrderCount);
        tag.putInt("generated_buy_order_count", generatedBuyOrderCount);
        tag.putInt("generated_sell_order_count", generatedSellOrderCount);
        tag.putInt("matched_trade_count", matchedTradeCount);
        tag.putDouble("generated_buy_liquidity", generatedBuyLiquidity);
        tag.putDouble("generated_sell_liquidity", generatedSellLiquidity);
        tag.putDouble("consumed_buy_liquidity", consumedBuyLiquidity);
        tag.putDouble("consumed_sell_liquidity", consumedSellLiquidity);
        return tag;
    }

    public void load(CompoundTag tag) {
        generatedOrderCount = tag.getInt("generated_order_count");
        generatedBuyOrderCount = tag.getInt("generated_buy_order_count");
        generatedSellOrderCount = tag.getInt("generated_sell_order_count");
        matchedTradeCount = tag.getInt("matched_trade_count");
        generatedBuyLiquidity = tag.getDouble("generated_buy_liquidity");
        generatedSellLiquidity = tag.getDouble("generated_sell_liquidity");
        consumedBuyLiquidity = tag.getDouble("consumed_buy_liquidity");
        consumedSellLiquidity = tag.getDouble("consumed_sell_liquidity");
    }
}
