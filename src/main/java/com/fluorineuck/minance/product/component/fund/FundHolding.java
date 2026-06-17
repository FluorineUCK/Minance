package com.fluorineuck.minance.product.fund;

import com.fluorineuck.minance.market.financial.FinancialProductType;
import net.minecraft.nbt.CompoundTag;

public record FundHolding(
        String productId,
        FinancialProductType productType,
        double quantity,
        long averageCost,
        long currentPrice
) {
    public FundHolding {
        productId = productId == null ? "" : productId;
        productType = productType == null ? FinancialProductType.STRUCTURED_PRODUCT : productType;
        quantity = Math.max(0.0D, quantity);
        averageCost = Math.max(0L, averageCost);
        currentPrice = Math.max(0L, currentPrice);
    }

    public double marketValue() {
        return quantity * currentPrice;
    }

    public double unrealizedPnl() {
        return quantity * (currentPrice - averageCost);
    }

    public FundHolding withCurrentPrice(long price) {
        return new FundHolding(productId, productType, quantity, averageCost, price);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("product_id", productId);
        tag.putString("product_type", productType.name());
        tag.putDouble("quantity", quantity);
        tag.putLong("average_cost", averageCost);
        tag.putLong("current_price", currentPrice);
        return tag;
    }

    public static FundHolding load(CompoundTag tag) {
        FinancialProductType type;
        try {
            type = FinancialProductType.valueOf(tag.getString("product_type"));
        } catch (IllegalArgumentException exception) {
            type = FinancialProductType.STRUCTURED_PRODUCT;
        }
        return new FundHolding(tag.getString("product_id"), type, tag.getDouble("quantity"), tag.getLong("average_cost"), tag.getLong("current_price"));
    }
}
