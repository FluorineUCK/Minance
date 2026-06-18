package com.fluorineuck.minance.market.index;

import com.fluorineuck.minance.config.MarketConfig;

import java.util.List;

public final class MarketIndexLevelCalculator {
    public static final MarketIndexLevelCalculator INSTANCE = new MarketIndexLevelCalculator();

    private MarketIndexLevelCalculator() {
    }

    public MarketIndexLevel calculate(MarketConfig.IndexDefinition definition, List<MarketIndexComponent> components, MarketConfig config) {
        if (components == null || components.isEmpty()) {
            return new MarketIndexLevel(1L, 0, 0.0D);
        }
        double rawWeightTotal = rawWeightTotal(definition, components);
        boolean useEqualFallback = rawWeightTotal <= 0.0D;
        double totalWeight = useEqualFallback ? components.size() : rawWeightTotal;
        double weightedTotal = 0.0D;
        for (MarketIndexComponent component : components) {
            double weight = componentWeight(definition, component, useEqualFallback);
            weightedTotal += adjustedPrice(component, config.index()) * weight;
        }
        long price = Math.max(1L, Math.round(weightedTotal / Math.max(1.0D, totalWeight)));
        return new MarketIndexLevel(price, components.size(), totalWeight);
    }

    private static double rawWeightTotal(MarketConfig.IndexDefinition definition, List<MarketIndexComponent> components) {
        return components.stream().mapToDouble(component -> rawWeight(definition, component)).sum();
    }

    private static double componentWeight(MarketConfig.IndexDefinition definition, MarketIndexComponent component, boolean useEqualFallback) {
        if (useEqualFallback) {
            return 1.0D;
        }
        return Math.max(0.0D, rawWeight(definition, component));
    }

    private static double rawWeight(MarketConfig.IndexDefinition definition, MarketIndexComponent component) {
        MarketIndexWeightingMethod method = definition.weightingMethod() == null ? MarketIndexWeightingMethod.EQUAL_WEIGHTED : definition.weightingMethod();
        return switch (method) {
            case VOLUME_WEIGHTED -> component.totalVolume();
            case ORDER_COUNT_WEIGHTED -> component.totalOrders();
            case EQUAL_WEIGHTED -> 1.0D;
        };
    }

    private static double adjustedPrice(MarketIndexComponent component, MarketConfig.IndexConfig index) {
        double volumeTotal = component.totalVolume();
        int orderTotal = component.totalOrders();
        if (volumeTotal <= 0.0D && orderTotal <= 0) {
            return component.basePrice();
        }
        double volumeImbalance = (component.buyVolume() - component.sellVolume()) / Math.max(1.0D, volumeTotal);
        double orderImbalance = (component.buyOrders() - component.sellOrders()) / (double) Math.max(1, orderTotal);
        double pressure = volumeImbalance * index.volumeImbalanceWeight() + orderImbalance * index.orderImbalanceWeight();
        double volatilityMultiplier = 1.0D + Math.min(index.volatilityMultiplierCap(), component.volatility());
        double maxMove = Math.max(0.0D, index.maxOrderMove());
        double move = pressure * Math.max(0.0D, index.orderPressureWeight()) * volatilityMultiplier;
        move = clamp(move, -maxMove, maxMove);
        return Math.max(1.0D, component.basePrice() * (1.0D + move));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
