package com.fluorineuck.minance.product.commodity.spot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record SpotMarketSource(
        SpotMarketSourceType sourceType,
        String sourceId,
        ResourceLocation item,
        double quantity,
        double price
) {
    public SpotMarketSource {
        sourceType = sourceType == null ? SpotMarketSourceType.EVENT : sourceType;
        sourceId = sourceId == null ? "" : sourceId;
        item = item == null ? ResourceLocation.withDefaultNamespace("air") : item;
        quantity = Math.max(0.0D, quantity);
        price = Math.max(0.0D, price);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("source_type", sourceType.getSerializedName());
        tag.putString("source_id", sourceId);
        tag.putString("item", item.toString());
        tag.putDouble("quantity", quantity);
        tag.putDouble("price", price);
        return tag;
    }

    public static SpotMarketSource load(CompoundTag tag) {
        ResourceLocation parsedItem = ResourceLocation.tryParse(tag.getString("item"));
        return new SpotMarketSource(
                SpotMarketSourceType.byName(tag.getString("source_type")),
                tag.getString("source_id"),
                parsedItem == null ? ResourceLocation.withDefaultNamespace("air") : parsedItem,
                tag.getDouble("quantity"),
                tag.getDouble("price")
        );
    }
}
