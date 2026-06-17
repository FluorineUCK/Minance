package com.fluorineuck.minance.product.commodity.core;

import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketAsset;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketSource;

import java.util.concurrent.ThreadLocalRandom;

public final class CommodityStabilizationDesk {
    public static final CommodityStabilizationDesk INSTANCE = new CommodityStabilizationDesk();

    private CommodityStabilizationDesk() {
    }

    public void maybeCreateCounterFlow(SpotMarketAsset asset, SpotMarketSource source, boolean originalSupply, MarketConfig config) {
        MarketConfig.Stabilizer stabilizer = config.stabilizer();
        if (!stabilizer.sourceTypes().contains(source.sourceType().getSerializedName())) {
            return;
        }
        double baseline = source.price() > 0.0D ? source.price() : asset.referencePrice();
        double deviation = Math.abs(asset.currentPrice() - baseline) / Math.max(1.0D, baseline);
        double probability = clamp(stabilizer.counterFlowBaseProbability() + deviation * stabilizer.counterFlowDeviationMultiplier(), 0.0D, stabilizer.counterFlowMaxProbability());
        if (ThreadLocalRandom.current().nextDouble() >= probability) {
            return;
        }
        double quantity = Math.max(stabilizer.minCounterFlowQuantity(), source.quantity() * clamp(stabilizer.quantityBaseMultiplier() + deviation, stabilizer.minQuantityMultiplier(), stabilizer.maxQuantityMultiplier()));
        if (asset.currentPrice() > baseline) {
            asset.recordStabilizerSell(quantity);
        } else if (asset.currentPrice() < baseline) {
            asset.recordStabilizerBuy(quantity);
        } else if (originalSupply) {
            asset.recordStabilizerBuy(quantity * stabilizer.neutralCounterFlowMultiplier());
        } else {
            asset.recordStabilizerSell(quantity * stabilizer.neutralCounterFlowMultiplier());
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
