package com.fluorineuck.minance.market.index;

public record MarketIndexComponent(
        String productId,
        double basePrice,
        double buyVolume,
        double sellVolume,
        int buyOrders,
        int sellOrders,
        double volatility
) {
    public MarketIndexComponent {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        basePrice = Math.max(1.0D, basePrice);
        buyVolume = Math.max(0.0D, buyVolume);
        sellVolume = Math.max(0.0D, sellVolume);
        buyOrders = Math.max(0, buyOrders);
        sellOrders = Math.max(0, sellOrders);
        volatility = Math.max(0.0D, volatility);
    }

    public double totalVolume() {
        return buyVolume + sellVolume;
    }

    public int totalOrders() {
        return buyOrders + sellOrders;
    }
}
