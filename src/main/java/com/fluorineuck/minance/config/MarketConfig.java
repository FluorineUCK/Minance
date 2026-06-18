package com.fluorineuck.minance.config;

import com.fluorineuck.minance.market.financial.PriceSignalSource;
import com.fluorineuck.minance.market.index.MarketIndexWeightingMethod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record MarketConfig(
        int settlementIntervalTicks,
        int indexUpdateIntervalTicks,
        int priceHistoryLimit,
        SpotPricing spot,
        Stabilizer stabilizer,
        FinancialMicrostructure financialMicrostructure,
        IndexConfig index,
        SignalWeights signalWeights
) {
    public static final Codec<MarketConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("settlement_interval_ticks").forGetter(MarketConfig::settlementIntervalTicks),
            Codec.INT.fieldOf("index_update_interval_ticks").forGetter(MarketConfig::indexUpdateIntervalTicks),
            Codec.INT.fieldOf("price_history_limit").forGetter(MarketConfig::priceHistoryLimit),
            SpotPricing.CODEC.fieldOf("spot").forGetter(MarketConfig::spot),
            Stabilizer.CODEC.fieldOf("stabilizer").forGetter(MarketConfig::stabilizer),
            FinancialMicrostructure.CODEC.fieldOf("financial_microstructure").forGetter(MarketConfig::financialMicrostructure),
            IndexConfig.CODEC.fieldOf("index").forGetter(MarketConfig::index),
            SignalWeights.CODEC.optionalFieldOf("signal_weights", SignalWeights.defaults()).forGetter(MarketConfig::signalWeights)
    ).apply(instance, MarketConfig::new));

    public static MarketConfig defaults() {
        return new MarketConfig(
                600,
                400,
                160,
                new SpotPricing(false, false, 0.18D, 0.12D, 0.02D, 0.20D, 0.08D, 1.0D, 1_000_000.0D),
                new Stabilizer(List.of("villager_buy_order", "villager_sell_order", "villager_production"), 0.04D, 0.75D, 0.85D, 1.0D, 0.25D, 0.25D, 1.0D, 0.5D, "villager_counter_flow"),
                new FinancialMicrostructure(
                        new PriceRules(1.0D, 1.0D, 1L),
                        new LiquidityImpact(0.035D, 10.0D, 1.0D, 0.15D, 0.08D),
                        new DecayRules(0.015D, 0.002D, 0.8D),
                        new VolatilityRules(0.001D, 0.9D, 0.1D),
                        new RadiusRules(2, 5, 8),
                        new LevelRules(0.12D, 0.08D, 0.001D, 1.0D, 3.0D, 0.01D)
                ),
                new IndexConfig(defaultIndices(), 0.75D, 0.25D, 0.10D, 0.06D, 1.0D),
                SignalWeights.defaults()
        );
    }

    public static List<IndexDefinition> defaultIndices() {
        return List.of(
                new IndexDefinition("food_index", "Food Index", List.of("bread", "beef", "porkchop", "chicken", "mutton", "cod", "salmon", "apple"), MarketIndexWeightingMethod.EQUAL_WEIGHTED, 0L, 0L),
                new IndexDefinition("crops_index", "Crops Index", List.of("wheat", "carrot", "potato", "beetroot", "melon", "pumpkin"), MarketIndexWeightingMethod.EQUAL_WEIGHTED, 0L, 0L),
                new IndexDefinition("minerals_index", "Minerals Index", List.of("iron", "coal", "copper", "gold", "diamond", "emerald", "redstone", "lapis"), MarketIndexWeightingMethod.EQUAL_WEIGHTED, 0L, 0L),
                new IndexDefinition("tools_index", "Tools Index", List.of("pickaxe", "axe", "shovel", "hoe", "sword", "helmet", "chestplate", "leggings", "boots"), MarketIndexWeightingMethod.EQUAL_WEIGHTED, 0L, 0L)
        );
    }

    public record SpotPricing(
            boolean allowVirtualSupply,
            boolean allowVirtualDemand,
            double inventoryWeight,
            double flowWeight,
            double stabilizerWeight,
            double adjustmentSpeed,
            double maxCycleChange,
            double minPrice,
            double maxPrice
    ) {
        public static final Codec<SpotPricing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("allow_virtual_supply").forGetter(SpotPricing::allowVirtualSupply),
                Codec.BOOL.fieldOf("allow_virtual_demand").forGetter(SpotPricing::allowVirtualDemand),
                Codec.DOUBLE.fieldOf("inventory_weight").forGetter(SpotPricing::inventoryWeight),
                Codec.DOUBLE.fieldOf("flow_weight").forGetter(SpotPricing::flowWeight),
                Codec.DOUBLE.fieldOf("stabilizer_weight").forGetter(SpotPricing::stabilizerWeight),
                Codec.DOUBLE.fieldOf("adjustment_speed").forGetter(SpotPricing::adjustmentSpeed),
                Codec.DOUBLE.fieldOf("max_cycle_change").forGetter(SpotPricing::maxCycleChange),
                Codec.DOUBLE.fieldOf("min_price").forGetter(SpotPricing::minPrice),
                Codec.DOUBLE.fieldOf("max_price").forGetter(SpotPricing::maxPrice)
        ).apply(instance, SpotPricing::new));
    }

    public record Stabilizer(
            List<String> sourceTypes,
            double counterFlowBaseProbability,
            double counterFlowDeviationMultiplier,
            double counterFlowMaxProbability,
            double minCounterFlowQuantity,
            double quantityBaseMultiplier,
            double minQuantityMultiplier,
            double maxQuantityMultiplier,
            double neutralCounterFlowMultiplier,
            String counterFlowSourceLabel
    ) {
        public static final Codec<Stabilizer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("source_types").forGetter(Stabilizer::sourceTypes),
                Codec.DOUBLE.fieldOf("counter_flow_base_probability").forGetter(Stabilizer::counterFlowBaseProbability),
                Codec.DOUBLE.fieldOf("counter_flow_deviation_multiplier").forGetter(Stabilizer::counterFlowDeviationMultiplier),
                Codec.DOUBLE.fieldOf("counter_flow_max_probability").forGetter(Stabilizer::counterFlowMaxProbability),
                Codec.DOUBLE.fieldOf("min_counter_flow_quantity").forGetter(Stabilizer::minCounterFlowQuantity),
                Codec.DOUBLE.fieldOf("quantity_base_multiplier").forGetter(Stabilizer::quantityBaseMultiplier),
                Codec.DOUBLE.fieldOf("min_quantity_multiplier").forGetter(Stabilizer::minQuantityMultiplier),
                Codec.DOUBLE.fieldOf("max_quantity_multiplier").forGetter(Stabilizer::maxQuantityMultiplier),
                Codec.DOUBLE.fieldOf("neutral_counter_flow_multiplier").forGetter(Stabilizer::neutralCounterFlowMultiplier),
                Codec.STRING.fieldOf("counter_flow_source_label").forGetter(Stabilizer::counterFlowSourceLabel)
        ).apply(instance, Stabilizer::new));
    }

    public record FinancialMicrostructure(
            PriceRules price,
            LiquidityImpact impact,
            DecayRules decay,
            VolatilityRules volatility,
            RadiusRules radius,
            LevelRules levels
    ) {
        public static final Codec<FinancialMicrostructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PriceRules.CODEC.fieldOf("price").forGetter(FinancialMicrostructure::price),
                LiquidityImpact.CODEC.fieldOf("impact").forGetter(FinancialMicrostructure::impact),
                DecayRules.CODEC.fieldOf("decay").forGetter(FinancialMicrostructure::decay),
                VolatilityRules.CODEC.fieldOf("volatility").forGetter(FinancialMicrostructure::volatility),
                RadiusRules.CODEC.fieldOf("radius").forGetter(FinancialMicrostructure::radius),
                LevelRules.CODEC.fieldOf("levels").forGetter(FinancialMicrostructure::levels)
        ).apply(instance, FinancialMicrostructure::new));

        public double tickSize() { return price.tickSize(); }
        public double minimumTickSize() { return price.minimumTickSize(); }
        public long minimumPrice() { return price.minimumPrice(); }
        public double liquidityImpact() { return impact.liquidityImpact(); }
        public double liquidityLogBase() { return impact.liquidityLogBase(); }
        public double minLiquidityDenominator() { return impact.minLiquidityDenominator(); }
        public double forceMoveImbalanceThreshold() { return impact.forceMoveImbalanceThreshold(); }
        public double consumedVolumeFactor() { return impact.consumedVolumeFactor(); }
        public double ageLambda() { return decay.ageLambda(); }
        public double distanceLambda() { return decay.distanceLambda(); }
        public double volatilityLambda() { return decay.volatilityLambda(); }
        public double minVolatility() { return volatility.minVolatility(); }
        public double realizedVolatilityPreviousWeight() { return volatility.realizedVolatilityPreviousWeight(); }
        public double realizedVolatilityChangeWeight() { return volatility.realizedVolatilityChangeWeight(); }
        public int minimumNearRadius() { return radius.minimumNearRadius(); }
        public int defaultNearRadius() { return radius.defaultNearRadius(); }
        public int optionNearRadius() { return radius.optionNearRadius(); }
        public double supportAgeWeight() { return levels.supportAgeWeight(); }
        public double supportTouchWeight() { return levels.supportTouchWeight(); }
        public double levelPruneLiquidity() { return levels.levelPruneLiquidity(); }
        public double initialLevelConfidence() { return levels.initialLevelConfidence(); }
        public double maxLevelConfidence() { return levels.maxLevelConfidence(); }
        public double confidencePerOrder() { return levels.confidencePerOrder(); }
    }

    public record PriceRules(
            double tickSize,
            double minimumTickSize,
            long minimumPrice
    ) {
        public static final Codec<PriceRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("tick_size").forGetter(PriceRules::tickSize),
                Codec.DOUBLE.fieldOf("minimum_tick_size").forGetter(PriceRules::minimumTickSize),
                Codec.LONG.fieldOf("minimum_price").forGetter(PriceRules::minimumPrice)
        ).apply(instance, PriceRules::new));
    }

    public record LiquidityImpact(
            double liquidityImpact,
            double liquidityLogBase,
            double minLiquidityDenominator,
            double forceMoveImbalanceThreshold,
            double consumedVolumeFactor
    ) {
        public static final Codec<LiquidityImpact> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("liquidity_impact").forGetter(LiquidityImpact::liquidityImpact),
                Codec.DOUBLE.fieldOf("liquidity_log_base").forGetter(LiquidityImpact::liquidityLogBase),
                Codec.DOUBLE.fieldOf("min_liquidity_denominator").forGetter(LiquidityImpact::minLiquidityDenominator),
                Codec.DOUBLE.fieldOf("force_move_imbalance_threshold").forGetter(LiquidityImpact::forceMoveImbalanceThreshold),
                Codec.DOUBLE.fieldOf("consumed_volume_factor").forGetter(LiquidityImpact::consumedVolumeFactor)
        ).apply(instance, LiquidityImpact::new));
    }

    public record DecayRules(
            double ageLambda,
            double distanceLambda,
            double volatilityLambda
    ) {
        public static final Codec<DecayRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("age_lambda").forGetter(DecayRules::ageLambda),
                Codec.DOUBLE.fieldOf("distance_lambda").forGetter(DecayRules::distanceLambda),
                Codec.DOUBLE.fieldOf("volatility_lambda").forGetter(DecayRules::volatilityLambda)
        ).apply(instance, DecayRules::new));
    }

    public record VolatilityRules(
            double minVolatility,
            double realizedVolatilityPreviousWeight,
            double realizedVolatilityChangeWeight
    ) {
        public static final Codec<VolatilityRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("min_volatility").forGetter(VolatilityRules::minVolatility),
                Codec.DOUBLE.fieldOf("realized_volatility_previous_weight").forGetter(VolatilityRules::realizedVolatilityPreviousWeight),
                Codec.DOUBLE.fieldOf("realized_volatility_change_weight").forGetter(VolatilityRules::realizedVolatilityChangeWeight)
        ).apply(instance, VolatilityRules::new));
    }

    public record RadiusRules(
            int minimumNearRadius,
            int defaultNearRadius,
            int optionNearRadius
    ) {
        public static final Codec<RadiusRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("minimum_near_radius").forGetter(RadiusRules::minimumNearRadius),
                Codec.INT.fieldOf("default_near_radius").forGetter(RadiusRules::defaultNearRadius),
                Codec.INT.fieldOf("option_near_radius").forGetter(RadiusRules::optionNearRadius)
        ).apply(instance, RadiusRules::new));
    }

    public record LevelRules(
            double supportAgeWeight,
            double supportTouchWeight,
            double levelPruneLiquidity,
            double initialLevelConfidence,
            double maxLevelConfidence,
            double confidencePerOrder
    ) {
        public static final Codec<LevelRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("support_age_weight").forGetter(LevelRules::supportAgeWeight),
                Codec.DOUBLE.fieldOf("support_touch_weight").forGetter(LevelRules::supportTouchWeight),
                Codec.DOUBLE.fieldOf("level_prune_liquidity").forGetter(LevelRules::levelPruneLiquidity),
                Codec.DOUBLE.fieldOf("initial_level_confidence").forGetter(LevelRules::initialLevelConfidence),
                Codec.DOUBLE.fieldOf("max_level_confidence").forGetter(LevelRules::maxLevelConfidence),
                Codec.DOUBLE.fieldOf("confidence_per_order").forGetter(LevelRules::confidencePerOrder)
        ).apply(instance, LevelRules::new));
    }

    public record IndexConfig(
            List<IndexDefinition> indices,
            double volumeImbalanceWeight,
            double orderImbalanceWeight,
            double orderPressureWeight,
            double maxOrderMove,
            double volatilityMultiplierCap
    ) {
        public static final Codec<IndexConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                IndexDefinition.CODEC.listOf().fieldOf("indices").forGetter(IndexConfig::indices),
                Codec.DOUBLE.fieldOf("volume_imbalance_weight").forGetter(IndexConfig::volumeImbalanceWeight),
                Codec.DOUBLE.fieldOf("order_imbalance_weight").forGetter(IndexConfig::orderImbalanceWeight),
                Codec.DOUBLE.fieldOf("order_pressure_weight").forGetter(IndexConfig::orderPressureWeight),
                Codec.DOUBLE.fieldOf("max_order_move").forGetter(IndexConfig::maxOrderMove),
                Codec.DOUBLE.fieldOf("volatility_multiplier_cap").forGetter(IndexConfig::volatilityMultiplierCap)
        ).apply(instance, IndexConfig::new));
    }

    public record IndexDefinition(
            String id,
            String name,
            List<String> matchers,
            MarketIndexWeightingMethod weightingMethod,
            long reconstitutionIntervalTicks,
            long rebalanceIntervalTicks
    ) {
        public IndexDefinition {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            name = name == null || name.isBlank() ? id : name;
            matchers = matchers == null ? List.of() : List.copyOf(matchers);
            weightingMethod = weightingMethod == null ? MarketIndexWeightingMethod.EQUAL_WEIGHTED : weightingMethod;
            reconstitutionIntervalTicks = Math.max(0L, reconstitutionIntervalTicks);
            rebalanceIntervalTicks = Math.max(0L, rebalanceIntervalTicks);
        }

        public static final Codec<IndexDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(IndexDefinition::id),
                Codec.STRING.fieldOf("name").forGetter(IndexDefinition::name),
                Codec.STRING.listOf().fieldOf("matchers").forGetter(IndexDefinition::matchers),
                MarketIndexWeightingMethod.CODEC.optionalFieldOf("weighting_method", MarketIndexWeightingMethod.EQUAL_WEIGHTED).forGetter(IndexDefinition::weightingMethod),
                Codec.LONG.optionalFieldOf("reconstitution_interval_ticks", 0L).forGetter(IndexDefinition::reconstitutionIntervalTicks),
                Codec.LONG.optionalFieldOf("rebalance_interval_ticks", 0L).forGetter(IndexDefinition::rebalanceIntervalTicks)
        ).apply(instance, IndexDefinition::new));
    }

    public record SignalWeights(
            double fundamentalWeight,
            double earningsWeight,
            double flowWeight,
            double riskWeight,
            double liquidityWeight,
            double sentimentWeight
    ) {
        public static final Codec<SignalWeights> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("fundamental_weight").forGetter(SignalWeights::fundamentalWeight),
                Codec.DOUBLE.fieldOf("earnings_weight").forGetter(SignalWeights::earningsWeight),
                Codec.DOUBLE.fieldOf("flow_weight").forGetter(SignalWeights::flowWeight),
                Codec.DOUBLE.fieldOf("risk_weight").forGetter(SignalWeights::riskWeight),
                Codec.DOUBLE.fieldOf("liquidity_weight").forGetter(SignalWeights::liquidityWeight),
                Codec.DOUBLE.fieldOf("sentiment_weight").forGetter(SignalWeights::sentimentWeight)
        ).apply(instance, SignalWeights::new));

        public static SignalWeights defaults() {
            return new SignalWeights(1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D);
        }

        public double weight(PriceSignalSource source) {
            return switch (source == null ? PriceSignalSource.UNKNOWN : source) {
                case FUNDAMENTAL -> fundamentalWeight;
                case EARNINGS -> earningsWeight;
                case ORDER_FLOW, PRODUCT_ADAPTER, COMMODITY_SPOT, INDEX -> flowWeight;
                case RISK -> riskWeight;
                case LIQUIDITY, INSTITUTION -> liquidityWeight;
                case SENTIMENT -> sentimentWeight;
                case UNKNOWN -> 1.0D;
            };
        }
    }
}
