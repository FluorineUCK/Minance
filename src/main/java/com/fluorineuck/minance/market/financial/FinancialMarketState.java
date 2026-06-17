package com.fluorineuck.minance.market.financial;

import com.fluorineuck.minance.config.ConfigRegistry;
import net.minecraft.nbt.CompoundTag;

public final class FinancialMarketState {
    private final String productId;
    private final FinancialProductType productType;
    private final LiquiditySurface surface = new LiquiditySurface();
    private final MarketActivityStats stats = new MarketActivityStats();
    private long currentPrice;
    private double realizedVolatility;
    private double lastImbalance;
    private double nearestBidLiquidity;
    private double nearestAskLiquidity;
    private long strongestSupportPrice;
    private long strongestResistancePrice;

    public FinancialMarketState(String productId, FinancialProductType productType, long currentPrice, double realizedVolatility) {
        this.productId = productId;
        this.productType = productType == null ? FinancialProductType.STRUCTURED_PRODUCT : productType;
        var config = ConfigRegistry.INSTANCE.market().financialMicrostructure();
        this.currentPrice = Math.max(config.minimumPrice(), currentPrice);
        this.realizedVolatility = Math.max(config.minVolatility(), realizedVolatility);
    }

    public String productId() { return productId; }
    public FinancialProductType productType() { return productType; }
    public LiquiditySurface surface() { return surface; }
    public MarketActivityStats stats() { return stats; }
    public long currentPrice() { return currentPrice; }
    public double realizedVolatility() { return realizedVolatility; }
    public double lastImbalance() { return lastImbalance; }
    public double nearestBidLiquidity() { return nearestBidLiquidity; }
    public double nearestAskLiquidity() { return nearestAskLiquidity; }
    public long strongestSupportPrice() { return strongestSupportPrice; }
    public long strongestResistancePrice() { return strongestResistancePrice; }

    public void updateDebug(long currentPrice, double realizedVolatility, double imbalance, double bid, double ask, long support, long resistance) {
        var config = ConfigRegistry.INSTANCE.market().financialMicrostructure();
        this.currentPrice = Math.max(config.minimumPrice(), currentPrice);
        this.realizedVolatility = Math.max(config.minVolatility(), realizedVolatility);
        this.lastImbalance = imbalance;
        this.nearestBidLiquidity = bid;
        this.nearestAskLiquidity = ask;
        this.strongestSupportPrice = support;
        this.strongestResistancePrice = resistance;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("product_id", productId);
        tag.putString("product_type", productType.name());
        tag.putLong("current_price", currentPrice);
        tag.putDouble("realized_volatility", realizedVolatility);
        tag.putDouble("last_imbalance", lastImbalance);
        tag.putDouble("nearest_bid_liquidity", nearestBidLiquidity);
        tag.putDouble("nearest_ask_liquidity", nearestAskLiquidity);
        tag.putLong("strongest_support_price", strongestSupportPrice);
        tag.putLong("strongest_resistance_price", strongestResistancePrice);
        tag.put("surface", surface.save());
        tag.put("stats", stats.save());
        return tag;
    }

    public static FinancialMarketState load(CompoundTag tag) {
        FinancialProductType type;
        try {
            type = FinancialProductType.valueOf(tag.getString("product_type"));
        } catch (IllegalArgumentException exception) {
            type = FinancialProductType.STRUCTURED_PRODUCT;
        }
        FinancialMarketState state = new FinancialMarketState(tag.getString("product_id"), type, tag.getLong("current_price"), tag.getDouble("realized_volatility"));
        state.lastImbalance = tag.getDouble("last_imbalance");
        state.nearestBidLiquidity = tag.getDouble("nearest_bid_liquidity");
        state.nearestAskLiquidity = tag.getDouble("nearest_ask_liquidity");
        state.strongestSupportPrice = tag.getLong("strongest_support_price");
        state.strongestResistancePrice = tag.getLong("strongest_resistance_price");
        state.surface.load(tag.getCompound("surface"));
        state.stats.load(tag.getCompound("stats"));
        return state;
    }
}
