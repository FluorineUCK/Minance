package com.fluorineuck.minance.client;

import com.fluorineuck.minance.Minance;
import com.fluorineuck.minance.client.ui.MarketLdLibDashboard;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Minance.MOD_ID, value = Dist.CLIENT)
public final class MinanceClientEvents {
    private static final String CATEGORY = "key.categories.minance";
    private static final KeyMapping OPEN_MARKET_DASHBOARD = new KeyMapping(
            "key.minance.open_market_dashboard",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY
    );

    private MinanceClientEvents() {
    }

    @EventBusSubscriber(modid = Minance.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_MARKET_DASHBOARD);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_MARKET_DASHBOARD.consumeClick()) {
            MarketLdLibDashboard.open();
        }
    }
}
