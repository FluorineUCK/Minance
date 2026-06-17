package com.fluorineuck.minance.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RiskConfig(
        double maxLeverage,
        double marginCallRatio,
        double liquidationRatio,
        double liabilitiesRatioThreshold,
        double liabilitiesPenaltyWeight,
        double baseDefaultProbability,
        double creditScoreIncomeWeight,
        double creditScoreLiquidityWeight,
        double creditScoreLeverageWeight
) {
    public static final Codec<RiskConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("max_leverage").forGetter(RiskConfig::maxLeverage),
            Codec.DOUBLE.fieldOf("margin_call_ratio").forGetter(RiskConfig::marginCallRatio),
            Codec.DOUBLE.fieldOf("liquidation_ratio").forGetter(RiskConfig::liquidationRatio),
            Codec.DOUBLE.fieldOf("liabilities_ratio_threshold").forGetter(RiskConfig::liabilitiesRatioThreshold),
            Codec.DOUBLE.fieldOf("liabilities_penalty_weight").forGetter(RiskConfig::liabilitiesPenaltyWeight),
            Codec.DOUBLE.fieldOf("base_default_probability").forGetter(RiskConfig::baseDefaultProbability),
            Codec.DOUBLE.fieldOf("credit_score_income_weight").forGetter(RiskConfig::creditScoreIncomeWeight),
            Codec.DOUBLE.fieldOf("credit_score_liquidity_weight").forGetter(RiskConfig::creditScoreLiquidityWeight),
            Codec.DOUBLE.fieldOf("credit_score_leverage_weight").forGetter(RiskConfig::creditScoreLeverageWeight)
    ).apply(instance, RiskConfig::new));

    public static RiskConfig defaults() {
        return new RiskConfig(3.0D, 0.35D, 0.25D, 0.70D, 0.30D, 0.01D, 0.45D, 0.35D, 0.20D);
    }
}
