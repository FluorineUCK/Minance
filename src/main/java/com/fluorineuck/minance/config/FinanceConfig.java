package com.fluorineuck.minance.config;

import com.fluorineuck.minance.market.financial.FinancialProductType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public record FinanceConfig(
        Map<String, ProductMarketParameters> products,
        DerivativePricing derivative,
        FundRules fund
) {
    public static final Codec<FinanceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ProductMarketParameters.CODEC).fieldOf("products").forGetter(FinanceConfig::products),
            DerivativePricing.CODEC.fieldOf("derivative").forGetter(FinanceConfig::derivative),
            FundRules.CODEC.fieldOf("fund").forGetter(FinanceConfig::fund)
    ).apply(instance, FinanceConfig::new));

    public static FinanceConfig defaults() {
        Map<String, ProductMarketParameters> products = new LinkedHashMap<>();
        products.put("equity", new ProductMarketParameters(0.08D, 18.0D, 0.65D, 0.75D, 0.75D, 0.0D));
        products.put("future", new ProductMarketParameters(0.10D, 12.0D, 1.0D, 1.0D, 1.0D, 0.20D));
        products.put("option", new ProductMarketParameters(0.18D, 7.0D, 1.45D, 1.35D, 1.4D, 0.55D));
        products.put("fund", new ProductMarketParameters(0.045D, 10.0D, 1.0D, 0.9D, 0.9D, 0.12D));
        products.put("bond", new ProductMarketParameters(0.025D, 5.0D, 0.45D, 0.55D, 0.45D, 0.05D));
        products.put("structured_product", new ProductMarketParameters(0.12D, 6.0D, 1.2D, 1.1D, 1.2D, 0.25D));
        return new FinanceConfig(
                Map.copyOf(products),
                new DerivativePricing(1, 100, 24_000L, 0.0005D, 0.05D, 0.001D, 100),
                new FundRules(8)
        );
    }

    public ProductMarketParameters product(FinancialProductType type) {
        ProductMarketParameters fallback = products.values().stream().findFirst().orElse(new ProductMarketParameters(0.05D, 1.0D, 1.0D, 1.0D, 1.0D, 0.0D));
        if (type == null) {
            return fallback;
        }
        return products.getOrDefault(type.name().toLowerCase(Locale.ROOT), fallback);
    }

    public double defaultVolatility(FinancialProductType type) {
        return product(type).defaultVolatility();
    }

    public record ProductMarketParameters(
            double defaultVolatility,
            double baseLiquidity,
            double decayAgeMultiplier,
            double decayDistanceMultiplier,
            double decayVolatilityMultiplier,
            double maturityLambda
    ) {
        public static final Codec<ProductMarketParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("default_volatility").forGetter(ProductMarketParameters::defaultVolatility),
                Codec.DOUBLE.fieldOf("base_liquidity").forGetter(ProductMarketParameters::baseLiquidity),
                Codec.DOUBLE.fieldOf("decay_age_multiplier").forGetter(ProductMarketParameters::decayAgeMultiplier),
                Codec.DOUBLE.fieldOf("decay_distance_multiplier").forGetter(ProductMarketParameters::decayDistanceMultiplier),
                Codec.DOUBLE.fieldOf("decay_volatility_multiplier").forGetter(ProductMarketParameters::decayVolatilityMultiplier),
                Codec.DOUBLE.fieldOf("maturity_lambda").forGetter(ProductMarketParameters::maturityLambda)
        ).apply(instance, ProductMarketParameters::new));
    }

    public record DerivativePricing(
            int maxDepth,
            int maxDays,
            long ticksPerMarketDay,
            double futuresCarryPerDay,
            double optionPremiumBaseRate,
            double optionPremiumPerDay,
            int optionPremiumMaxDays
    ) {
        public static final Codec<DerivativePricing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("max_depth").forGetter(DerivativePricing::maxDepth),
                Codec.INT.fieldOf("max_days").forGetter(DerivativePricing::maxDays),
                Codec.LONG.fieldOf("ticks_per_market_day").forGetter(DerivativePricing::ticksPerMarketDay),
                Codec.DOUBLE.fieldOf("futures_carry_per_day").forGetter(DerivativePricing::futuresCarryPerDay),
                Codec.DOUBLE.fieldOf("option_premium_base_rate").forGetter(DerivativePricing::optionPremiumBaseRate),
                Codec.DOUBLE.fieldOf("option_premium_per_day").forGetter(DerivativePricing::optionPremiumPerDay),
                Codec.INT.fieldOf("option_premium_max_days").forGetter(DerivativePricing::optionPremiumMaxDays)
        ).apply(instance, DerivativePricing::new));
    }

    public record FundRules(int indexTrackingMaxEquities) {
        public static final Codec<FundRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("index_tracking_max_equities").forGetter(FundRules::indexTrackingMaxEquities)
        ).apply(instance, FundRules::new));
    }
}
