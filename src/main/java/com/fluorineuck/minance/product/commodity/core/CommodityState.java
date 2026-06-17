package com.fluorineuck.minance.product.commodity.core;

import com.fluorineuck.minance.config.ConfigRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.Tag;

public class CommodityState {
    private final ResourceLocation item;

    private double inventory;
    private double targetInventory;
    private double productionRate;
    private double consumptionRate;
    private double inflow;
    private double outflow;
    private double perishRate;
    private double storageCost;
    private double referencePrice;

    public CommodityState(ResourceLocation item, double referencePrice) {
        this.item = item == null ? ResourceLocation.withDefaultNamespace("air") : item;
        this.referencePrice = Math.max(1.0D, referencePrice);
        this.targetInventory = ConfigRegistry.INSTANCE.commodity().initialTargetInventory();
    }

    public ResourceLocation item() {
        return item;
    }

    public double inventory() {
        return inventory;
    }

    public int roundedInventory() {
        return (int) Math.round(inventory);
    }

    public void setInventory(double inventory) {
        this.inventory = Math.max(0.0D, inventory);
    }

    public double targetInventory() {
        return targetInventory;
    }

    public void setTargetInventory(double targetInventory) {
        this.targetInventory = Math.max(1.0D, targetInventory);
    }

    public double productionRate() {
        return productionRate;
    }

    public double consumptionRate() {
        return consumptionRate;
    }

    public double inflow() {
        return inflow;
    }

    public double outflow() {
        return outflow;
    }

    public double perishRate() {
        return perishRate;
    }

    public void setPerishRate(double perishRate) {
        this.perishRate = Math.max(0.0D, perishRate);
    }

    public double storageCost() {
        return storageCost;
    }

    public void setStorageCost(double storageCost) {
        this.storageCost = Math.max(0.0D, storageCost);
    }

    public double referencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(double referencePrice) {
        this.referencePrice = Math.max(1.0D, referencePrice);
    }

    public void produce(double quantity) {
        if (quantity <= 0.0D) {
            return;
        }
        productionRate += quantity;
        receiveInflow(quantity);
    }

    public void consume(double quantity) {
        if (quantity <= 0.0D) {
            return;
        }
        consumptionRate += quantity;
        sendOutflow(quantity);
    }

    public void receiveInflow(double quantity) {
        if (quantity <= 0.0D) {
            return;
        }
        inflow += quantity;
        inventory += quantity;
    }

    public void sendOutflow(double quantity) {
        if (quantity <= 0.0D) {
            return;
        }
        outflow += quantity;
        inventory = Math.max(0.0D, inventory - quantity);
    }

    public void applySpoilage() {
        if (perishRate <= 0.0D || inventory <= 0.0D) {
            return;
        }
        double spoiled = Math.min(inventory, inventory * perishRate);
        inventory -= spoiled;
        outflow += spoiled;
    }

    public void resetCycleCounters() {
        productionRate = 0.0D;
        consumptionRate = 0.0D;
        inflow = 0.0D;
        outflow = 0.0D;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("item", item.toString());
        tag.putDouble("inventory", inventory);
        tag.putDouble("target_inventory", targetInventory);
        tag.putDouble("production_rate", productionRate);
        tag.putDouble("consumption_rate", consumptionRate);
        tag.putDouble("inflow", inflow);
        tag.putDouble("outflow", outflow);
        tag.putDouble("perish_rate", perishRate);
        tag.putDouble("storage_cost", storageCost);
        tag.putDouble("reference_price", referencePrice);
        return tag;
    }

    public static CommodityState load(CompoundTag tag) {
        ResourceLocation parsedItem = ResourceLocation.tryParse(tag.getString("item"));
        CommodityState state = new CommodityState(
                parsedItem == null ? ResourceLocation.withDefaultNamespace("air") : parsedItem,
                tag.contains("reference_price", Tag.TAG_DOUBLE) ? tag.getDouble("reference_price") : tag.getLong("price")
        );
        state.inventory = tag.contains("inventory", Tag.TAG_DOUBLE) ? tag.getDouble("inventory") : tag.getInt("inventory");
        state.targetInventory = tag.contains("target_inventory", Tag.TAG_DOUBLE) ? Math.max(1.0D, tag.getDouble("target_inventory")) : ConfigRegistry.INSTANCE.commodity().initialTargetInventory();
        state.productionRate = tag.getDouble("production_rate");
        state.consumptionRate = tag.getDouble("consumption_rate");
        state.inflow = tag.getDouble("inflow");
        state.outflow = tag.getDouble("outflow");
        state.perishRate = tag.getDouble("perish_rate");
        state.storageCost = tag.getDouble("storage_cost");
        return state;
    }
}
