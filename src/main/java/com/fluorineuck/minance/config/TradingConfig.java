package com.fluorineuck.minance.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record TradingConfig(
        int discoveryCacheLimit,
        double professionFlowQuantity,
        List<ResourceLocation> relevantVillageBlocks,
        OrderFlow orderFlow
) {
    public static final Codec<TradingConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("discovery_cache_limit").forGetter(TradingConfig::discoveryCacheLimit),
            Codec.DOUBLE.fieldOf("profession_flow_quantity").forGetter(TradingConfig::professionFlowQuantity),
            ResourceLocation.CODEC.listOf().fieldOf("relevant_village_blocks").forGetter(TradingConfig::relevantVillageBlocks),
            OrderFlow.CODEC.fieldOf("order_flow").forGetter(TradingConfig::orderFlow)
    ).apply(instance, TradingConfig::new));

    public static TradingConfig defaults() {
        return new TradingConfig(
                8192,
                1.0D,
                List.of(
                        ResourceLocation.withDefaultNamespace("bell"),
                        ResourceLocation.withDefaultNamespace("composter"),
                        ResourceLocation.withDefaultNamespace("barrel"),
                        ResourceLocation.withDefaultNamespace("blast_furnace"),
                        ResourceLocation.withDefaultNamespace("smoker"),
                        ResourceLocation.withDefaultNamespace("fletching_table"),
                        ResourceLocation.withDefaultNamespace("cartography_table"),
                        ResourceLocation.withDefaultNamespace("brewing_stand"),
                        ResourceLocation.withDefaultNamespace("cauldron"),
                        ResourceLocation.withDefaultNamespace("lectern"),
                        ResourceLocation.withDefaultNamespace("stonecutter"),
                        ResourceLocation.withDefaultNamespace("loom"),
                        ResourceLocation.withDefaultNamespace("grindstone"),
                        ResourceLocation.withDefaultNamespace("smithing_table")
                ),
                OrderFlow.defaults()
        );
    }

    public record OrderFlow(
            int injectedOrderSplitDivisor,
            int minimumInjectedOrders,
            RetailNoise retailNoise,
            LiquidityBand marketMaker,
            LiquidityBand anchoring,
            ThresholdFlow momentum,
            MeanReversionFlow meanReversion,
            ThresholdFlow herding,
            PanicFlow panic,
            AnchorFollowerFlow anchorFollower,
            ExpiringOptionFlow expiringOption
    ) {
        public static final Codec<OrderFlow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("injected_order_split_divisor").forGetter(OrderFlow::injectedOrderSplitDivisor),
                Codec.INT.fieldOf("minimum_injected_orders").forGetter(OrderFlow::minimumInjectedOrders),
                RetailNoise.CODEC.fieldOf("retail_noise").forGetter(OrderFlow::retailNoise),
                LiquidityBand.CODEC.fieldOf("market_maker").forGetter(OrderFlow::marketMaker),
                LiquidityBand.CODEC.fieldOf("anchoring").forGetter(OrderFlow::anchoring),
                ThresholdFlow.CODEC.fieldOf("momentum").forGetter(OrderFlow::momentum),
                MeanReversionFlow.CODEC.fieldOf("mean_reversion").forGetter(OrderFlow::meanReversion),
                ThresholdFlow.CODEC.fieldOf("herding").forGetter(OrderFlow::herding),
                PanicFlow.CODEC.fieldOf("panic").forGetter(OrderFlow::panic),
                AnchorFollowerFlow.CODEC.fieldOf("anchor_follower").forGetter(OrderFlow::anchorFollower),
                ExpiringOptionFlow.CODEC.fieldOf("expiring_option").forGetter(OrderFlow::expiringOption)
        ).apply(instance, OrderFlow::new));

        public static OrderFlow defaults() {
            return new OrderFlow(
                    2,
                    1,
                    new RetailNoise(1.0D, -2L, 3L, 0.3D, 1.1D, 1, 4),
                    new LiquidityBand(1.0D, 1, 0.9D, 2),
                    new LiquidityBand(1.0D, 1, 0.45D, 1),
                    new ThresholdFlow(1.0D, 0.05D, 1, 0.65D, 2),
                    new MeanReversionFlow(1.0D, 2, 1, 0.75D, 2),
                    new ThresholdFlow(1.0D, 0.25D, 2, 0.5D, 2),
                    new PanicFlow(1.0D, 0.08D, 3, 0.8D, 3),
                    new AnchorFollowerFlow(1.0D, 0.55D, 2),
                    new ExpiringOptionFlow(1.0D, 7, 0.25D, 2)
            );
        }
    }

    public record RetailNoise(
            double participationRate,
            long minTickOffset,
            long maxTickOffsetExclusive,
            double minLiquidityMultiplier,
            double maxLiquidityMultiplier,
            int minOrders,
            int maxOrdersExclusive
    ) {
        public static final Codec<RetailNoise> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("participation_rate").forGetter(RetailNoise::participationRate),
                Codec.LONG.fieldOf("min_tick_offset").forGetter(RetailNoise::minTickOffset),
                Codec.LONG.fieldOf("max_tick_offset_exclusive").forGetter(RetailNoise::maxTickOffsetExclusive),
                Codec.DOUBLE.fieldOf("min_liquidity_multiplier").forGetter(RetailNoise::minLiquidityMultiplier),
                Codec.DOUBLE.fieldOf("max_liquidity_multiplier").forGetter(RetailNoise::maxLiquidityMultiplier),
                Codec.INT.fieldOf("min_orders").forGetter(RetailNoise::minOrders),
                Codec.INT.fieldOf("max_orders_exclusive").forGetter(RetailNoise::maxOrdersExclusive)
        ).apply(instance, RetailNoise::new));
    }

    public record LiquidityBand(double participationRate, int tickOffset, double liquidityMultiplier, int orderCount) {
        public static final Codec<LiquidityBand> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("participation_rate").forGetter(LiquidityBand::participationRate),
                Codec.INT.fieldOf("tick_offset").forGetter(LiquidityBand::tickOffset),
                Codec.DOUBLE.fieldOf("liquidity_multiplier").forGetter(LiquidityBand::liquidityMultiplier),
                Codec.INT.fieldOf("order_count").forGetter(LiquidityBand::orderCount)
        ).apply(instance, LiquidityBand::new));
    }

    public record ThresholdFlow(double participationRate, double threshold, int tickOffset, double liquidityMultiplier, int orderCount) {
        public static final Codec<ThresholdFlow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("participation_rate").forGetter(ThresholdFlow::participationRate),
                Codec.DOUBLE.fieldOf("threshold").forGetter(ThresholdFlow::threshold),
                Codec.INT.fieldOf("tick_offset").forGetter(ThresholdFlow::tickOffset),
                Codec.DOUBLE.fieldOf("liquidity_multiplier").forGetter(ThresholdFlow::liquidityMultiplier),
                Codec.INT.fieldOf("order_count").forGetter(ThresholdFlow::orderCount)
        ).apply(instance, ThresholdFlow::new));
    }

    public record MeanReversionFlow(double participationRate, int anchorDistance, int tickOffset, double liquidityMultiplier, int orderCount) {
        public static final Codec<MeanReversionFlow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("participation_rate").forGetter(MeanReversionFlow::participationRate),
                Codec.INT.fieldOf("anchor_distance").forGetter(MeanReversionFlow::anchorDistance),
                Codec.INT.fieldOf("tick_offset").forGetter(MeanReversionFlow::tickOffset),
                Codec.DOUBLE.fieldOf("liquidity_multiplier").forGetter(MeanReversionFlow::liquidityMultiplier),
                Codec.INT.fieldOf("order_count").forGetter(MeanReversionFlow::orderCount)
        ).apply(instance, MeanReversionFlow::new));
    }

    public record PanicFlow(double participationRate, double volatilityThreshold, int tickOffset, double liquidityMultiplier, int orderCount) {
        public static final Codec<PanicFlow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("participation_rate").forGetter(PanicFlow::participationRate),
                Codec.DOUBLE.fieldOf("volatility_threshold").forGetter(PanicFlow::volatilityThreshold),
                Codec.INT.fieldOf("tick_offset").forGetter(PanicFlow::tickOffset),
                Codec.DOUBLE.fieldOf("liquidity_multiplier").forGetter(PanicFlow::liquidityMultiplier),
                Codec.INT.fieldOf("order_count").forGetter(PanicFlow::orderCount)
        ).apply(instance, PanicFlow::new));
    }

    public record AnchorFollowerFlow(double participationRate, double liquidityMultiplier, int orderCount) {
        public static final Codec<AnchorFollowerFlow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("participation_rate").forGetter(AnchorFollowerFlow::participationRate),
                Codec.DOUBLE.fieldOf("liquidity_multiplier").forGetter(AnchorFollowerFlow::liquidityMultiplier),
                Codec.INT.fieldOf("order_count").forGetter(AnchorFollowerFlow::orderCount)
        ).apply(instance, AnchorFollowerFlow::new));
    }

    public record ExpiringOptionFlow(double participationRate, int daysThreshold, double liquidityMultiplier, int orderCount) {
        public static final Codec<ExpiringOptionFlow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("participation_rate").forGetter(ExpiringOptionFlow::participationRate),
                Codec.INT.fieldOf("days_threshold").forGetter(ExpiringOptionFlow::daysThreshold),
                Codec.DOUBLE.fieldOf("liquidity_multiplier").forGetter(ExpiringOptionFlow::liquidityMultiplier),
                Codec.INT.fieldOf("order_count").forGetter(ExpiringOptionFlow::orderCount)
        ).apply(instance, ExpiringOptionFlow::new));
    }
}
