package com.fluorineuck.minance;

import com.fluorineuck.minance.command.MinanceCommands;
import com.fluorineuck.minance.data.MinanceReloaders;
import com.fluorineuck.minance.entity.villager.VillagerTradeDiscoveryService;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Minance.MOD_ID)
public final class Minance {
    public static final String MOD_ID = "minance";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Minance(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(MinanceReloaders::registerReloaders);
        NeoForge.EVENT_BUS.addListener(MinanceCommands::register);
        NeoForge.EVENT_BUS.addListener(VillagerTradeDiscoveryService::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(VillagerTradeDiscoveryService::onPlayerEntityInteract);
        NeoForge.EVENT_BUS.addListener(VillagerTradeDiscoveryService::onTradeWithVillager);
        NeoForge.EVENT_BUS.addListener(VillagerTradeDiscoveryService::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(VillagerTradeDiscoveryService::onServerTick);
    }
}