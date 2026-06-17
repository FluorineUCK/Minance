package com.fluorineuck.minance.product.derivative;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class OptionContract {
    private final long id;
    private final String marketId;
    private final String underlyingProductId;
    private final OptionRight right;
    private final int quantity;
    private final long strikePrice;
    private final long openPremium;
    private long currentPremium;
    private final long openTick;
    private final long expirationTick;
    private final long margin;
    private final DerivativeSide side;
    private final String holder;
    private DerivativeContractStatus status;
    private long realizedPnl;

    public OptionContract(long id, String marketId, String underlyingProductId, OptionRight right, int quantity, long strikePrice, long openPremium, long currentPremium, long openTick, long expirationTick, long margin, DerivativeSide side, String holder, DerivativeContractStatus status, long realizedPnl) {
        this.id = id;
        this.marketId = marketId;
        this.underlyingProductId = underlyingProductId == null || underlyingProductId.isBlank() ? "minecraft:air" : underlyingProductId;
        this.right = right == null ? OptionRight.CALL : right;
        this.quantity = Math.max(1, quantity);
        this.strikePrice = Math.max(1L, strikePrice);
        this.openPremium = Math.max(1L, openPremium);
        this.currentPremium = Math.max(1L, currentPremium);
        this.openTick = openTick;
        this.expirationTick = expirationTick;
        this.margin = Math.max(0L, margin);
        this.side = side == null ? DerivativeSide.BUY : side;
        this.holder = holder == null ? "" : holder;
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
    public OptionRight right() { return right; }
    public int quantity() { return quantity; }
    public long strikePrice() { return strikePrice; }
    public long openPremium() { return openPremium; }
    public long currentPremium() { return currentPremium; }
    public void setCurrentPremium(long currentPremium) { this.currentPremium = Math.max(1L, currentPremium); }
    public long openTick() { return openTick; }
    public long expirationTick() { return expirationTick; }
    public long margin() { return margin; }
    public DerivativeSide side() { return side; }
    public String holder() { return holder; }
    public DerivativeContractStatus status() { return status; }
    public void setStatus(DerivativeContractStatus status) { this.status = status == null ? DerivativeContractStatus.OPEN : status; }
    public long realizedPnl() { return realizedPnl; }
    public void setRealizedPnl(long realizedPnl) { this.realizedPnl = realizedPnl; }

    public long unrealizedPnl() {
        long delta = (currentPremium - openPremium) * (long) quantity;
        return side == DerivativeSide.BUY ? delta : -delta;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("id", id);
        tag.putString("market", marketId);
        tag.putString("underlying_product", underlyingProductId);
        tag.putString("right", right.getSerializedName());
        tag.putInt("quantity", quantity);
        tag.putLong("strike_price", strikePrice);
        tag.putLong("open_premium", openPremium);
        tag.putLong("current_premium", currentPremium);
        tag.putLong("open_tick", openTick);
        tag.putLong("expiration_tick", expirationTick);
        tag.putLong("margin", margin);
        tag.putString("side", side.getSerializedName());
        tag.putString("holder", holder);
        tag.putString("status", status.getSerializedName());
        tag.putLong("realized_pnl", realizedPnl);
        return tag;
    }

    public static OptionContract load(CompoundTag tag) {
        String underlying = tag.contains("underlying_product", Tag.TAG_STRING) ? tag.getString("underlying_product") : tag.getString("commodity");
        return new OptionContract(
                tag.getLong("id"),
                tag.getString("market"),
                underlying,
                OptionRight.byName(tag.getString("right")),
                tag.getInt("quantity"),
                tag.getLong("strike_price"),
                tag.getLong("open_premium"),
                tag.getLong("current_premium"),
                tag.getLong("open_tick"),
                tag.getLong("expiration_tick"),
                tag.getLong("margin"),
                DerivativeSide.byName(tag.getString("side")),
                tag.getString("holder"),
                DerivativeContractStatus.byName(tag.getString("status")),
                tag.getLong("realized_pnl")
        );
    }
}
