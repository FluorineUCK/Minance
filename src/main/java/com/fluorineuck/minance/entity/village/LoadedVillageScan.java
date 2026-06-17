package com.fluorineuck.minance.entity.village;

import com.fluorineuck.minance.config.ConfigRegistry;
import com.fluorineuck.minance.rule.MinanceRuleRegistries;
import com.fluorineuck.minance.rule.ProfessionTradeCategoryRule;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class LoadedVillageScan {
    private final String id;
    private final ResourceLocation dimension;
    private final BlockPos bellPos;
    private final Map<ResourceLocation, Integer> professionCounts = new LinkedHashMap<>();
    private final Map<UUID, ResourceLocation> members = new LinkedHashMap<>();
    private long productionIncome;
    private double proxyLiquidityDemand;
    private double derivativeDemand;

    public LoadedVillageScan(String id, ResourceLocation dimension, BlockPos bellPos) {
        this.id = id;
        this.dimension = dimension;
        this.bellPos = bellPos;
    }

    public String id() {
        return id;
    }

    public ResourceLocation dimension() {
        return dimension;
    }

    public BlockPos bellPos() {
        return bellPos;
    }

    public Map<ResourceLocation, Integer> professionCounts() {
        return professionCounts;
    }

    public Map<UUID, ResourceLocation> members() {
        return members;
    }

    public long productionIncome() {
        return productionIncome;
    }

    public double proxyLiquidityDemand() {
        return proxyLiquidityDemand;
    }

    public double derivativeDemand() {
        return derivativeDemand;
    }

    public void add(Villager villager, ResourceLocation profession) {
        professionCounts.merge(profession, 1, Integer::sum);
        members.put(villager.getUUID(), profession);
        ProfessionTradeCategoryRule rule = MinanceRuleRegistries.INSTANCE.profession(profession).orElse(null);
        if (rule == null) {
            proxyLiquidityDemand += ConfigRegistry.INSTANCE.economy().fallbackProxyLiquidityDemand();
            return;
        }
        productionIncome += Math.max(0L, rule.productionIncome());
        double baseDemand = Math.max(0.0D, rule.riskPreference()) * Math.max(1, rule.expectedShares());
        proxyLiquidityDemand += baseDemand;
        if (villager.getVillagerData().getLevel() >= Math.max(rule.derivativeProxyLevel(), ConfigRegistry.INSTANCE.company().seniorVillagerLevel())) {
            derivativeDemand += baseDemand * Math.max(0.0D, rule.derivativeDemandWeight()) * Math.max(0.0D, ConfigRegistry.INSTANCE.company().seniorDerivativeDemandMultiplier());
        }
    }
}
