package com.fluorineuck.minance.entity.villager;

import com.fluorineuck.minance.config.CompanyConfig;
import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.entity.company.VillageCompanyService;
import com.fluorineuck.minance.rule.MinanceRuleRegistries;
import com.fluorineuck.minance.rule.ProfessionTradeCategoryRule;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketService;
import com.fluorineuck.minance.market.index.MarketIndexService;
import com.fluorineuck.minance.product.fund.FundService;
import com.fluorineuck.minance.product.commodity.spot.SpotMarketSourceType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class VillagerTradeDiscoveryService {
    public static final VillagerTradeDiscoveryService INSTANCE = new VillagerTradeDiscoveryService();

    private final Set<String> discoveredKeys = new LinkedHashSet<>();
    private volatile boolean discoveryRequested = true;
    private long lastScanTick = -1L;
    private long lastSettlementTick = -1L;
    private long lastIndexUpdateTick = -1L;

    private VillagerTradeDiscoveryService() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Villager villager) {
            INSTANCE.discoverVillager(villager, true);
            INSTANCE.discoveryRequested = true;
        }
    }

    public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide() && event.getTarget() instanceof Villager villager) {
            INSTANCE.discoverVillager(villager, true);
            INSTANCE.discoveryRequested = true;
        }
    }

    public static void onTradeWithVillager(TradeWithVillagerEvent event) {
        AbstractVillager abstractVillager = event.getAbstractVillager();
        if (abstractVillager.level().isClientSide() || !(abstractVillager instanceof Villager villager)) {
            return;
        }
        INSTANCE.discoverOffer(villager, event.getMerchantOffer(), false);
        INSTANCE.discoverVillager(villager, true);
        INSTANCE.discoveryRequested = true;
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel && isRelevantVillageBlock(event.getState())) {
            INSTANCE.discoveryRequested = true;
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        INSTANCE.tick(event.getServer());
    }

    public void tick(MinecraftServer server) {
        MarketConfig config = ConfigRegistry.INSTANCE.market();
        CompanyConfig companyConfig = ConfigRegistry.INSTANCE.company();
        long now = server.getTickCount();
        if (discoveryRequested || lastScanTick < 0L || now - lastScanTick >= Math.max(1, companyConfig.loadedVillageScanIntervalTicks())) {
            scanLoadedVillagers(server);
            VillageCompanyService.INSTANCE.scanLoadedVillages(server, now);
            discoveryRequested = false;
            lastScanTick = now;
        }
        boolean settlementDue = lastSettlementTick < 0L || now - lastSettlementTick >= Math.max(1, config.settlementIntervalTicks());
        if (settlementDue) {
            SpotMarketService.INSTANCE.updatePrices();
            MarketIndexService.INSTANCE.updateFromSpotMarket();
            FundService.INSTANCE.updateAllFunds();
            lastSettlementTick = now;
            lastIndexUpdateTick = now;
        } else if (lastIndexUpdateTick < 0L || now - lastIndexUpdateTick >= Math.max(1, config.indexUpdateIntervalTicks())) {
            MarketIndexService.INSTANCE.updateFromSpotMarket();
            lastIndexUpdateTick = now;
        }
    }

    public void scanLoadedVillagers(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            List<? extends Villager> villagers = level.getEntities(EntityTypeTest.forClass(Villager.class), Villager::isAlive);
            for (Villager villager : villagers) {
                discoverVillager(villager, true);
            }
        }
    }

    private void discoverVillager(Villager villager, boolean dedupe) {
        discoverProfessionCategories(villager, dedupe);
        for (MerchantOffer offer : villager.getOffers()) {
            discoverOffer(villager, offer, dedupe);
        }
    }

    private void discoverProfessionCategories(Villager villager, boolean dedupe) {
        ResourceLocation profession = professionId(villager);
        Optional<ProfessionTradeCategoryRule> maybeRule = MinanceRuleRegistries.INSTANCE.profession(profession);
        if (maybeRule.isEmpty()) {
            return;
        }
        String key = "profession:" + villager.getUUID() + ":" + profession;
        if (dedupe && !markDiscovered(key)) {
            return;
        }
        ProfessionTradeCategoryRule rule = maybeRule.get();
        String sourceId = "villager:" + villager.getUUID();
        long price = defaultPrice();
        for (ResourceLocation item : rule.productItems()) {
            SpotMarketService.INSTANCE.recordSupply(item, SpotMarketSourceType.VILLAGER_PRODUCTION, sourceId, ConfigRegistry.INSTANCE.trading().professionFlowQuantity(), price);
        }
        for (ResourceLocation item : rule.demandItems()) {
            SpotMarketService.INSTANCE.recordDemand(item, SpotMarketSourceType.VILLAGER_BUY_ORDER, sourceId, ConfigRegistry.INSTANCE.trading().professionFlowQuantity(), price);
        }
    }

    private void discoverOffer(Villager villager, MerchantOffer offer, boolean dedupe) {
        String sourceId = "villager:" + villager.getUUID();
        String key = sourceId + ":offer:" + offer.getCostA() + ":" + offer.getCostB() + ":" + offer.getResult();
        if (dedupe && !markDiscovered(key)) {
            return;
        }
        long emeraldValue = emeraldFundsInOffer(offer);
        recordOfferDemand(sourceId, offer.getCostA(), emeraldValue);
        recordOfferDemand(sourceId, offer.getCostB(), emeraldValue);
        recordOfferSupply(sourceId, offer.getResult(), emeraldValue);
    }

    private void recordOfferDemand(String sourceId, ItemStack stack, long emeraldValue) {
        if (stack.isEmpty() || stack.is(Items.EMERALD)) {
            return;
        }
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        double quantity = Math.max(1, stack.getCount());
        double unitPrice = emeraldValue > 0L ? emeraldValue / quantity : defaultPrice();
        SpotMarketService.INSTANCE.recordDemand(item, SpotMarketSourceType.VILLAGER_BUY_ORDER, sourceId, quantity, unitPrice);
    }

    private void recordOfferSupply(String sourceId, ItemStack stack, long emeraldValue) {
        if (stack.isEmpty() || stack.is(Items.EMERALD)) {
            return;
        }
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        double quantity = Math.max(1, stack.getCount());
        double unitPrice = emeraldValue > 0L ? emeraldValue / quantity : defaultPrice();
        SpotMarketService.INSTANCE.recordSupply(item, SpotMarketSourceType.VILLAGER_SELL_ORDER, sourceId, quantity, unitPrice);
    }

    private boolean markDiscovered(String key) {
        if (discoveredKeys.contains(key)) {
            return false;
        }
        discoveredKeys.add(key);
        while (discoveredKeys.size() > Math.max(0, ConfigRegistry.INSTANCE.trading().discoveryCacheLimit())) {
            Iterator<String> iterator = discoveredKeys.iterator();
            if (!iterator.hasNext()) {
                break;
            }
            iterator.next();
            iterator.remove();
        }
        return true;
    }

    private long emeraldFundsInOffer(MerchantOffer offer) {
        int emeralds = emeraldCount(offer.getCostA()) + emeraldCount(offer.getCostB()) + emeraldCount(offer.getResult());
        return emeralds * Math.max(0L, ConfigRegistry.INSTANCE.economy().emeraldFundValue());
    }

    private int emeraldCount(ItemStack stack) {
        return stack.is(Items.EMERALD) ? stack.getCount() : 0;
    }

    private long defaultPrice() {
        return Math.max(1L, ConfigRegistry.INSTANCE.economy().defaultItemPrice());
    }

    private static ResourceLocation professionId(Villager villager) {
        ResourceLocation id = BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession());
        return id == null ? ResourceLocation.withDefaultNamespace("none") : id;
    }

    private static boolean isRelevantVillageBlock(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return ConfigRegistry.INSTANCE.trading().relevantVillageBlocks().contains(blockId);
    }
}


