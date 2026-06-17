package com.fluorineuck.minance.product.commodity.spot;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.market.financial.MarketFlowSnapshot;
import com.fluorineuck.minance.market.financial.MarketFlowSnapshot.FlowActor;
import com.fluorineuck.minance.product.commodity.core.CommodityState;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.Deque;

public final class SpotMarketAsset {
    private final CommodityState commodity;
    private final SpotMarketEngine spotEngine;
    private MarketFlowSnapshot flowSnapshot = MarketFlowSnapshot.EMPTY;
    private double volatility;
    private int buyOrderCount;
    private int sellOrderCount;
    private Map<String, Double> cycleSupplyBreakdown = new LinkedHashMap<>();
    private Map<String, Double> cycleDemandBreakdown = new LinkedHashMap<>();
    private Map<String, Double> lastSupplyBreakdown = new LinkedHashMap<>();
    private Map<String, Double> lastDemandBreakdown = new LinkedHashMap<>();

    public SpotMarketAsset(ResourceLocation item, long price, double volatility) {
        this(new CommodityState(item, price), price, volatility);
    }

    private SpotMarketAsset(CommodityState commodity, long price, double volatility) {
        this.commodity = commodity;
        this.spotEngine = new SpotMarketEngine(commodity, price);
        this.volatility = Math.max(0.0D, volatility);
    }

    public CommodityState commodity() {
        return commodity;
    }

    public SpotMarketEngine spotEngine() {
        return spotEngine;
    }

    public MarketFlowSnapshot flowSnapshot() {
        return flowSnapshot;
    }

    public ResourceLocation item() {
        return commodity.item();
    }

    public long price() {
        return spotEngine.price();
    }

    public double referencePrice() {
        return commodity.referencePrice();
    }

    public double currentPrice() {
        return spotEngine.currentPrice();
    }

    public double nextPrice() {
        return spotEngine.nextPrice();
    }

    public long previousPrice() {
        return spotEngine.previousPrice();
    }

    public Deque<Long> priceHistory() {
        return new ArrayDeque<>(spotEngine.priceHistory());
    }

    public int inventory() {
        return commodity.roundedInventory();
    }

    public double inventoryValue() {
        return commodity.inventory();
    }

    public void setInventory(int inventory) {
        commodity.setInventory(inventory);
    }

    public double targetInventory() {
        return commodity.targetInventory();
    }

    public void setTargetInventory(double targetInventory) {
        commodity.setTargetInventory(targetInventory);
    }

    public int volume() {
        return (int) Math.round(flowSnapshot.buyVolume() + flowSnapshot.sellVolume());
    }

    public void addVolume(int amount) {
        if (amount > 0) {
            flowSnapshot = flowSnapshot.recordBuy(amount, FlowActor.COMPANY);
        }
    }

    public void resetVolume() {
        resetCycleCounters();
    }

    public double volatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = Math.max(0.0D, volatility);
    }

    public double inflow() {
        return commodity.inflow();
    }

    public double outflow() {
        return commodity.outflow();
    }

    public double playerBuyVolume() {
        return Math.max(0.0D, flowSnapshot.playerFlow());
    }

    public double playerSellVolume() {
        return Math.max(0.0D, -flowSnapshot.playerFlow());
    }

    public double companyBuyVolume() {
        return Math.max(0.0D, flowSnapshot.companyFlow());
    }

    public double companySellVolume() {
        return Math.max(0.0D, -flowSnapshot.companyFlow());
    }

    public double stabilizerBuyVolume() {
        return Math.max(0.0D, flowSnapshot.stabilizerFlow());
    }

    public double stabilizerSellVolume() {
        return Math.max(0.0D, -flowSnapshot.stabilizerFlow());
    }

    public int orderCount() {
        return flowSnapshot.tradeCount();
    }

    public int buyOrderCount() {
        return buyOrderCount;
    }

    public int sellOrderCount() {
        return sellOrderCount;
    }

    public double supplyQuantity() {
        return flowSnapshot.sellVolume();
    }

    public double demandQuantity() {
        return flowSnapshot.buyVolume();
    }

    public Map<String, Double> supplyBreakdown() {
        return Map.copyOf(lastSupplyBreakdown.isEmpty() ? cycleSupplyBreakdown : lastSupplyBreakdown);
    }

    public Map<String, Double> demandBreakdown() {
        return Map.copyOf(lastDemandBreakdown.isEmpty() ? cycleDemandBreakdown : lastDemandBreakdown);
    }

    public void recordSupply(SpotMarketSource source) {
        if (invalid(source)) {
            return;
        }
        commodity.setReferencePrice(weightedReference(commodity.referencePrice(), source.price()));
        sellOrderCount++;
        if (source.sourceType() == SpotMarketSourceType.VILLAGER_PRODUCTION) {
            commodity.produce(source.quantity());
        } else {
            commodity.receiveInflow(source.quantity());
        }
        flowSnapshot = flowSnapshot.recordSell(source.quantity(), actorFor(source.sourceType()));
        merge(cycleSupplyBreakdown, source.sourceType().getSerializedName(), source.quantity());
    }

    public void recordDemand(SpotMarketSource source) {
        if (invalid(source)) {
            return;
        }
        commodity.setReferencePrice(weightedReference(commodity.referencePrice(), source.price()));
        buyOrderCount++;
        commodity.consume(source.quantity());
        flowSnapshot = flowSnapshot.recordBuy(source.quantity(), actorFor(source.sourceType()));
        merge(cycleDemandBreakdown, source.sourceType().getSerializedName(), source.quantity());
    }

    public void recordStabilizerBuy(double quantity) {
        if (quantity <= 0.0D) {
            return;
        }
        buyOrderCount++;
        flowSnapshot = flowSnapshot.recordBuy(quantity, FlowActor.STABILIZER);
        merge(cycleDemandBreakdown, ConfigRegistry.INSTANCE.market().stabilizer().counterFlowSourceLabel(), quantity);
    }

    public void recordStabilizerSell(double quantity) {
        if (quantity <= 0.0D) {
            return;
        }
        sellOrderCount++;
        flowSnapshot = flowSnapshot.recordSell(quantity, FlowActor.STABILIZER);
        merge(cycleSupplyBreakdown, ConfigRegistry.INSTANCE.market().stabilizer().counterFlowSourceLabel(), quantity);
    }

    public void applyNextPrice(double nextPrice, int historyLimit) {
        spotEngine.applyNextPrice(nextPrice, historyLimit);
    }

    public void settle(MarketConfig config) {
        spotEngine.settle(flowSnapshot, config, ConfigRegistry.INSTANCE.commodity());
        resetCycleCounters();
    }

    public void resetCycleCounters() {
        lastSupplyBreakdown = new LinkedHashMap<>(cycleSupplyBreakdown);
        lastDemandBreakdown = new LinkedHashMap<>(cycleDemandBreakdown);
        commodity.resetCycleCounters();
        flowSnapshot = MarketFlowSnapshot.EMPTY;
        buyOrderCount = 0;
        sellOrderCount = 0;
        cycleSupplyBreakdown = new LinkedHashMap<>();
        cycleDemandBreakdown = new LinkedHashMap<>();
    }

    public void setPrice(long price, int historyLimit) {
        applyNextPrice(price, historyLimit);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("item", item().toString());
        tag.putLong("price", price());
        tag.putDouble("reference_price", referencePrice());
        tag.putDouble("current_price", currentPrice());
        tag.putDouble("next_price", nextPrice());
        tag.putDouble("inventory", inventoryValue());
        tag.putDouble("target_inventory", targetInventory());
        tag.putDouble("volatility", volatility);
        tag.put("commodity", commodity.save());
        tag.put("spot", spotEngine.save());
        tag.put("last_supply", saveBreakdown(lastSupplyBreakdown));
        tag.put("last_demand", saveBreakdown(lastDemandBreakdown));
        return tag;
    }

    public static SpotMarketAsset load(CompoundTag tag) {
        CommodityState commodity = tag.contains("commodity", Tag.TAG_COMPOUND)
                ? CommodityState.load(tag.getCompound("commodity"))
                : CommodityState.load(tag);
        SpotMarketAsset asset = new SpotMarketAsset(commodity, Math.max(1L, tag.getLong("price")), tag.getDouble("volatility"));
        asset.spotEngine.load(tag.contains("spot", Tag.TAG_COMPOUND) ? tag.getCompound("spot") : tag);
        asset.lastSupplyBreakdown = loadBreakdown(tag.getList("last_supply", Tag.TAG_COMPOUND));
        asset.lastDemandBreakdown = loadBreakdown(tag.getList("last_demand", Tag.TAG_COMPOUND));
        return asset;
    }

    private static boolean invalid(SpotMarketSource source) {
        return source == null || source.quantity() <= 0.0D || source.item().equals(ResourceLocation.withDefaultNamespace("air"));
    }

    private static FlowActor actorFor(SpotMarketSourceType sourceType) {
        return switch (sourceType) {
            case PLAYER_ORDER -> FlowActor.PLAYER;
            case COMPANY_ORDER, EXPORT, IMPORT -> FlowActor.COMPANY;
            default -> FlowActor.OTHER;
        };
    }

    private static double weightedReference(double current, double observed) {
        if (observed <= 0.0D) {
            return current;
        }
        double observedWeight = clamp(ConfigRegistry.INSTANCE.commodity().referenceObservedWeight(), 0.0D, 1.0D);
        return Math.max(1.0D, current * (1.0D - observedWeight) + observed * observedWeight);
    }

    private static void merge(Map<String, Double> target, String key, double value) {
        target.merge(key, value, Double::sum);
    }

    private static ListTag saveBreakdown(Map<String, Double> values) {
        ListTag list = new ListTag();
        values.forEach((key, value) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("key", key);
            tag.putDouble("value", value);
            list.add(tag);
        });
        return list;
    }

    private static Map<String, Double> loadBreakdown(ListTag list) {
        Map<String, Double> values = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            values.put(tag.getString("key"), tag.getDouble("value"));
        }
        return values;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
