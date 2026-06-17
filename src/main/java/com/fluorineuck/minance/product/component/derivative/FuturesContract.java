package com.fluorineuck.minance.product.derivative;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class FuturesContract {
    private final long id;
    private final String marketId;
    private final String underlyingProductId;
    private final int quantity;
    private final long openPrice;
    private long currentPrice;
    private final long openTick;
    private final long expirationTick;
    private final long margin;
    private final DerivativeSide side;
    private final String holder;
    private final DerivativeDeliveryMethod deliveryMethod;
    private DerivativeContractStatus status;
    private long realizedPnl;

    public FuturesContract(long id, String marketId, String underlyingProductId, int quantity, long openPrice, long currentPrice, long openTick, long expirationTick, long margin, DerivativeSide side, String holder, DerivativeDeliveryMethod deliveryMethod, DerivativeContractStatus status, long realizedPnl) {
        this.id = id;
        this.marketId = marketId;
        this.underlyingProductId = underlyingProductId == null || underlyingProductId.isBlank() ? "minecraft:air" : underlyingProductId;
        this.quantity = Math.max(1, quantity);
        this.openPrice = Math.max(1L, openPrice);
        this.currentPrice = Math.max(1L, currentPrice);
        this.openTick = openTick;
        this.expirationTick = expirationTick;
        this.margin = Math.max(0L, margin);
        this.side = side == null ? DerivativeSide.BUY : side;
        this.holder = holder == null ? "" : holder;
        this.deliveryMethod = deliveryMethod == null ? DerivativeDeliveryMethod.CASH_SETTLEMENT : deliveryMethod;
        this.status = status == null ? DerivativeContractStatus.OPEN : status;
        this.realizedPnl = realizedPnl;
    }

    public long id() { return id; }
    public String marketId() { return marketId; }
    public String underlyingProductId() { return underlyingProductId; }
    public ResourceLocation commodity() {
        ResourceLocation parsed = ResourceLocation.tryParse(underlyingProductId);
        return parsed == null ? ResourceLocation.withDefaultNamespace("air") : parsed;
    }
    public int quantity() { return quantity; }
    public long openPrice() { return openPrice; }
    public long currentPrice() { return currentPrice; }
    public void setCurrentPrice(long currentPrice) { this.currentPrice = Math.max(1L, currentPrice); }
    public long openTick() { return openTick; }
    public long expirationTick() { return expirationTick; }
    public long margin() { return margin; }
    public DerivativeSide side() { return side; }
    public String holder() { return holder; }
    public DerivativeDeliveryMethod deliveryMethod() { return deliveryMethod; }
    public DerivativeContractStatus status() { return status; }
    public void setStatus(DerivativeContractStatus status) { this.status = status == null ? DerivativeContractStatus.OPEN : status; }
    public long realizedPnl() { return realizedPnl; }
    public void setRealizedPnl(long realizedPnl) { this.realizedPnl = realizedPnl; }

    public long unrealizedPnl() {
        long delta = (currentPrice - openPrice) * (long) quantity;
        return side == DerivativeSide.BUY ? delta : -delta;
    }

    public boolean open() {
        return status == DerivativeContractStatus.OPEN;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("id", id);
        tag.putString("market", marketId);
        tag.putString("underlying_product", underlyingProductId);
        tag.putInt("quantity", quantity);
        tag.putLong("open_price", openPrice);
        tag.putLong("current_price", currentPrice);
        tag.putLong("open_tick", openTick);
        tag.putLong("expiration_tick", expirationTick);
        tag.putLong("margin", margin);
        tag.putString("side", side.getSerializedName());
        tag.putString("holder", holder);
        tag.putString("delivery_method", deliveryMethod.getSerializedName());
        tag.putString("status", status.getSerializedName());
        tag.putLong("realized_pnl", realizedPnl);
        return tag;
    }

    public static FuturesContract load(CompoundTag tag) {
        String underlying = tag.contains("underlying_product", Tag.TAG_STRING) ? tag.getString("underlying_product") : tag.getString("commodity");
        return new FuturesContract(
                tag.getLong("id"),
                tag.getString("market"),
                underlying,
                tag.getInt("quantity"),
                tag.getLong("open_price"),
                tag.getLong("current_price"),
                tag.getLong("open_tick"),
                tag.getLong("expiration_tick"),
                tag.getLong("margin"),
                DerivativeSide.byName(tag.getString("side")),
                tag.getString("holder"),
                DerivativeDeliveryMethod.byName(tag.getString("delivery_method")),
                DerivativeContractStatus.byName(tag.getString("status")),
                tag.getLong("realized_pnl")
        );
    }
}
