package com.fluorineuck.minance.market.index;

import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketAsset;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class MarketIndexComponentCollector {
    private MarketIndexComponentCollector() {
    }

    static List<MarketIndexComponent> collect(MarketConfig.IndexDefinition definition, Collection<SpotMarketAsset> assets) {
        return assets.stream()
                .filter(asset -> matches(definition, asset))
                .map(MarketIndexComponentCollector::component)
                .toList();
    }

    static List<MarketIndexComponent> collectByIds(Collection<String> componentIds, Collection<SpotMarketAsset> assets) {
        Set<String> ids = componentIds == null ? Set.of() : Set.copyOf(componentIds);
        if (ids.isEmpty()) {
            return List.of();
        }
        return assets.stream()
                .filter(asset -> ids.contains(productId(asset)))
                .map(MarketIndexComponentCollector::component)
                .toList();
    }

    private static boolean matches(MarketConfig.IndexDefinition definition, SpotMarketAsset asset) {
        String itemPath = asset.item().getPath().toLowerCase(Locale.ROOT);
        return definition.matchers().stream().anyMatch(itemPath::contains);
    }

    private static MarketIndexComponent component(SpotMarketAsset asset) {
        return new MarketIndexComponent(
                productId(asset),
                asset.currentPrice(),
                asset.demandQuantity(),
                asset.supplyQuantity(),
                asset.buyOrderCount(),
                asset.sellOrderCount(),
                asset.volatility()
        );
    }

    private static String productId(SpotMarketAsset asset) {
        return asset.item().toString();
    }
}
