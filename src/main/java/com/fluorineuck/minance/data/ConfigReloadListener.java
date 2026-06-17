package com.fluorineuck.minance.data;

import com.fluorineuck.minance.Minance;
import com.fluorineuck.minance.config.CommodityConfig;
import com.fluorineuck.minance.config.CompanyConfig;
import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.config.EconomyConfig;
import com.fluorineuck.minance.config.FinanceConfig;
import com.fluorineuck.minance.config.MarketConfig;
import com.fluorineuck.minance.config.RiskConfig;
import com.fluorineuck.minance.config.TradingConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.Optional;

public final class ConfigReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public ConfigReloadListener() {
        super(GSON, "config");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        MarketConfig market = decode(objects, "market", MarketConfig.CODEC).orElse(MarketConfig.defaults());
        CommodityConfig commodity = decode(objects, "commodity", CommodityConfig.CODEC).orElse(CommodityConfig.defaults());
        FinanceConfig finance = decode(objects, "finance", FinanceConfig.CODEC).orElse(FinanceConfig.defaults());
        CompanyConfig company = decode(objects, "company", CompanyConfig.CODEC).orElse(CompanyConfig.defaults());
        TradingConfig trading = decode(objects, "trading", TradingConfig.CODEC).orElse(TradingConfig.defaults());
        RiskConfig risk = decode(objects, "risk", RiskConfig.CODEC).orElse(RiskConfig.defaults());
        EconomyConfig economy = decode(objects, "economy", EconomyConfig.CODEC).orElse(EconomyConfig.defaults());
        ConfigRegistry.INSTANCE.replaceAll(market, commodity, finance, company, trading, risk, economy);
        Minance.LOGGER.info("Loaded Minance config set from datapack config/");
    }

    private static <T> Optional<T> decode(Map<ResourceLocation, JsonElement> objects, String path, Codec<T> codec) {
        JsonElement json = objects.get(ResourceLocation.fromNamespaceAndPath(Minance.MOD_ID, path));
        if (json == null) {
            return Optional.empty();
        }
        return codec.parse(JsonOps.INSTANCE, json).resultOrPartial(message ->
                Minance.LOGGER.warn("Failed to load Minance config {}: {}", path, message)
        );
    }
}
