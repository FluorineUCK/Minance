package com.fluorineuck.minance.market.index;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MarketIndexService {
    public static final MarketIndexService INSTANCE = new MarketIndexService();

    private final Map<String, MarketIndexState> indices = new LinkedHashMap<>();

    private MarketIndexService() {
        ensureDefaults();
    }

    public Map<String, MarketIndexState> indices() {
        ensureDefaults();
        return indices;
    }

    public List<MarketIndexState> sortedIndices() {
        ensureDefaults();
        return indices.values().stream().sorted(java.util.Comparator.comparing(MarketIndexState::id)).toList();
    }

    public void updateFromSpotMarket() {
        updateFromSpotMarket(-1L);
    }

    public void updateFromSpotMarket(long gameTime) {
        ensureDefaults();
        for (MarketConfig.IndexDefinition definition : ConfigRegistry.INSTANCE.market().index().indices()) {
            update(definition, gameTime);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        indices.values().forEach(index -> list.add(index.save()));
        tag.put("indices", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        indices.clear();
        ListTag list = tag.getList("indices", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            MarketIndexState index = MarketIndexState.load(list.getCompound(i));
            indices.put(index.id(), index);
        }
        ensureDefaults();
    }

    private void update(MarketConfig.IndexDefinition definition, long gameTime) {
        MarketConfig config = ConfigRegistry.INSTANCE.market();
        MarketIndexState state = indices.computeIfAbsent(definition.id(), ignored -> new MarketIndexState(definition.id(), definition.name(), 1L));
        boolean reconstitutionDue = cadenceDue(gameTime, state.lastReconstitutionTick(), definition.reconstitutionIntervalTicks()) || state.componentIds().isEmpty();
        boolean rebalanceDue = cadenceDue(gameTime, state.lastRebalanceTick(), definition.rebalanceIntervalTicks());
        if (!reconstitutionDue && !rebalanceDue) {
            return;
        }
        List<MarketIndexComponent> components = reconstitutionDue
                ? MarketIndexComponentCollector.collect(definition, SpotMarketService.INSTANCE.assets().values())
                : MarketIndexComponentCollector.collectByIds(state.componentIds(), SpotMarketService.INSTANCE.assets().values());
        if (components.isEmpty()) {
            return;
        }
        MarketIndexLevel level = MarketIndexLevelCalculator.INSTANCE.calculate(definition, components, config);
        List<String> componentIds = components.stream().map(MarketIndexComponent::productId).toList();
        long reconstitutionTick = reconstitutionDue ? gameTime : state.lastReconstitutionTick();
        long rebalanceTick = rebalanceDue || reconstitutionDue ? gameTime : state.lastRebalanceTick();
        state.update(level.price(), level.componentCount(), componentIds, config.priceHistoryLimit(), reconstitutionTick, rebalanceTick);
    }

    private static boolean cadenceDue(long gameTime, long lastTick, long intervalTicks) {
        return gameTime < 0L || lastTick < 0L || gameTime - lastTick >= Math.max(1L, intervalTicks);
    }

    private void ensureDefaults() {
        for (MarketConfig.IndexDefinition definition : ConfigRegistry.INSTANCE.market().index().indices()) {
            indices.computeIfAbsent(definition.id(), ignored -> new MarketIndexState(definition.id(), definition.name(), 1L));
        }
    }
}

