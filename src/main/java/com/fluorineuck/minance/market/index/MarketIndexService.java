package com.fluorineuck.minance.market.index;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketAsset;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MarketIndexService {
    public static final MarketIndexService INSTANCE = new MarketIndexService();

    private final Map<String, MarketIndexState> indices = new LinkedHashMap<>();

    private MarketIndexService() {
        ensureDefaults();
    }

    public Map<String, MarketIndexState> indices() {
        ensureDefaults();
        return indices;
    }

    public List<MarketIndexState> sortedIndices() {
        ensureDefaults();
        return indices.values().stream().sorted(java.util.Comparator.comparing(MarketIndexState::id)).toList();
    }

    public void updateFromSpotMarket() {
        ensureDefaults();
        for (MarketConfig.IndexDefinition definition : ConfigRegistry.INSTANCE.market().index().indices()) {
            update(definition);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        indices.values().forEach(index -> list.add(index.save()));
        tag.put("indices", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        indices.clear();
        ListTag list = tag.getList("indices", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            MarketIndexState index = MarketIndexState.load(list.getCompound(i));
            indices.put(index.id(), index);
        }
        ensureDefaults();
    }

    private void update(MarketConfig.IndexDefinition definition) {
        double total = 0.0D;
        int count = 0;
        MarketConfig config = ConfigRegistry.INSTANCE.market();
        for (SpotMarketAsset asset : SpotMarketService.INSTANCE.assets().values()) {
            String item = asset.item().getPath().toLowerCase(Locale.ROOT);
            if (definition.matchers().stream().anyMatch(item::contains)) {
                total += indexComponentPrice(asset, config);
                count++;
            }
        }
        if (count <= 0) {
            return;
        }
        long price = Math.max(1L, Math.round(total / count));
        indices.computeIfAbsent(definition.id(), ignored -> new MarketIndexState(definition.id(), definition.name(), price))
                .update(price, count, config.priceHistoryLimit());
    }

    private static double indexComponentPrice(SpotMarketAsset asset, MarketConfig config) {
        double basePrice = Math.max(1.0D, asset.currentPrice());
        double buyVolume = asset.demandQuantity();
        double sellVolume = asset.supplyQuantity();
        int buyOrders = asset.buyOrderCount();
        int sellOrders = asset.sellOrderCount();
        double volumeTotal = buyVolume + sellVolume;
        int orderTotal = buyOrders + sellOrders;
        if (volumeTotal <= 0.0D && orderTotal <= 0) {
            return basePrice;
        }
        double volumeImbalance = (buyVolume - sellVolume) / Math.max(1.0D, volumeTotal);
        double orderImbalance = (buyOrders - sellOrders) / (double) Math.max(1, orderTotal);
        MarketConfig.IndexConfig index = config.index();
        double pressure = volumeImbalance * index.volumeImbalanceWeight() + orderImbalanceContribution(index, orderImbalance);
        double volatilityMultiplier = 1.0D + Math.min(index.volatilityMultiplierCap(), Math.max(0.0D, asset.volatility()));
        double maxMove = Math.max(0.0D, index.maxOrderMove());
        double move = pressure * Math.max(0.0D, index.orderPressureWeight()) * volatilityMultiplier;
        move = clamp(move, -maxMove, maxMove);
        return Math.max(1.0D, basePrice * (1.0D + move));
    }

    private static double orderImbalanceContribution(MarketConfig.IndexConfig index, double orderImbalance) {
        return orderImbalance * index.orderImbalanceWeight();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void ensureDefaults() {
        for (MarketConfig.IndexDefinition definition : ConfigRegistry.INSTANCE.market().index().indices()) {
            indices.computeIfAbsent(definition.id(), ignored -> new MarketIndexState(definition.id(), definition.name(), 1L));
        }
    }
}

