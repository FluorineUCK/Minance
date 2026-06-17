package com.fluorineuck.minance.product.commodity.spot;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.product.commodity.core.CommodityStabilizationDesk;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpotMarketService {
    public static final SpotMarketService INSTANCE = new SpotMarketService();

    private final Map<ResourceLocation, SpotMarketAsset> assets = new LinkedHashMap<>();

    private SpotMarketService() {
    }

    public Map<ResourceLocation, SpotMarketAsset> assets() {
        return assets;
    }


    public SpotMarketAsset ensureAsset(ResourceLocation item, long initialPrice, double volatility) {
        return assets.computeIfAbsent(item, key -> new SpotMarketAsset(key, initialPrice, volatility));
    }

    public void replaceAssets(Map<ResourceLocation, SpotMarketAsset> loadedAssets) {
        assets.clear();
        assets.putAll(loadedAssets);
    }

    public void recordSupply(ResourceLocation item, SpotMarketSourceType sourceType, String sourceId, double quantity, double price) {
        SpotMarketAsset asset = ensureAsset(item, Math.max(1L, Math.round(price)), ConfigRegistry.INSTANCE.commodity().defaultVolatility());
        SpotMarketSource source = new SpotMarketSource(sourceType, sourceId, item, quantity, price);
        asset.recordSupply(source);
        CommodityStabilizationDesk.INSTANCE.maybeCreateCounterFlow(asset, source, true, ConfigRegistry.INSTANCE.market());
    }

    public void recordDemand(ResourceLocation item, SpotMarketSourceType sourceType, String sourceId, double quantity, double price) {
        SpotMarketAsset asset = ensureAsset(item, Math.max(1L, Math.round(price)), ConfigRegistry.INSTANCE.commodity().defaultVolatility());
        SpotMarketSource source = new SpotMarketSource(sourceType, sourceId, item, quantity, price);
        asset.recordDemand(source);
        CommodityStabilizationDesk.INSTANCE.maybeCreateCounterFlow(asset, source, false, ConfigRegistry.INSTANCE.market());
    }

    public void updatePrices() {
        MarketConfig config = ConfigRegistry.INSTANCE.market();
        for (SpotMarketAsset asset : assets.values()) {
            if (!config.spot().allowVirtualSupply() && !config.spot().allowVirtualDemand() && asset.supplyQuantity() <= 0.0D && asset.demandQuantity() <= 0.0D) {
                continue;
            }
            asset.settle(config);
        }
    }


    public List<SpotMarketRow> rows() {
        return assets.values().stream()
                .sorted(Comparator.comparing(asset -> asset.item().toString()))
                .map(asset -> new SpotMarketRow(
                        asset.item(),
                        asset.price(),
                        asset.previousPrice(),
                        asset.volume(),
                        asset.inventory(),
                        asset.volatility(),
                        asset.supplyBreakdown(),
                        asset.demandBreakdown()
                ))
                .toList();
    }
}
