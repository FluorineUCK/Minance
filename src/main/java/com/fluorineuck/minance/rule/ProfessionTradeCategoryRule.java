package com.fluorineuck.minance.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ProfessionTradeCategoryRule(
        ResourceLocation id,
        ResourceLocation profession,
        int expectedShares,
        List<ResourceLocation> productItems,
        List<ResourceLocation> demandItems,
        long productionIncome,
        long targetProfit,
        double riskPreference,
        double spotDemandBudgetRatio,
        int derivativeProxyLevel,
        double derivativeDemandWeight
) {
    public static final Codec<ProfessionTradeCategoryRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(ProfessionTradeCategoryRule::id),
            ResourceLocation.CODEC.fieldOf("profession").forGetter(ProfessionTradeCategoryRule::profession),
            Codec.INT.fieldOf("expected_shares").forGetter(ProfessionTradeCategoryRule::expectedShares),
            ResourceLocation.CODEC.listOf().optionalFieldOf("product_items", List.of()).forGetter(ProfessionTradeCategoryRule::productItems),
            ResourceLocation.CODEC.listOf().optionalFieldOf("demand_items", List.of()).forGetter(ProfessionTradeCategoryRule::demandItems),
            Codec.LONG.fieldOf("production_income").forGetter(ProfessionTradeCategoryRule::productionIncome),
            Codec.LONG.fieldOf("target_profit").forGetter(ProfessionTradeCategoryRule::targetProfit),
            Codec.DOUBLE.fieldOf("risk_preference").forGetter(ProfessionTradeCategoryRule::riskPreference),
            Codec.DOUBLE.fieldOf("spot_demand_budget_ratio").forGetter(ProfessionTradeCategoryRule::spotDemandBudgetRatio),
            Codec.INT.fieldOf("derivative_proxy_level").forGetter(ProfessionTradeCategoryRule::derivativeProxyLevel),
            Codec.DOUBLE.fieldOf("derivative_demand_weight").forGetter(ProfessionTradeCategoryRule::derivativeDemandWeight)
    ).apply(instance, ProfessionTradeCategoryRule::new));
}
