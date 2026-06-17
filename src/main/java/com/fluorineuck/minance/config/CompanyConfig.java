package com.fluorineuck.minance.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CompanyConfig(
        long minimumCapitalPerShare,
        long initialVillageFunds,
        int bellSearchRadius,
        int bellSearchVerticalRadius,
        int loadedVillageScanIntervalTicks,
        int priceHistoryLimit,
        int financialReportIntervalTicks,
        int financialReportHistoryLimit,
        int seniorVillagerLevel,
        double seniorDerivativeDemandMultiplier,
        ReportRules report
) {
    public static final Codec<CompanyConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("minimum_capital_per_share").forGetter(CompanyConfig::minimumCapitalPerShare),
            Codec.LONG.fieldOf("initial_village_funds").forGetter(CompanyConfig::initialVillageFunds),
            Codec.INT.fieldOf("bell_search_radius").forGetter(CompanyConfig::bellSearchRadius),
            Codec.INT.fieldOf("bell_search_vertical_radius").forGetter(CompanyConfig::bellSearchVerticalRadius),
            Codec.INT.fieldOf("loaded_village_scan_interval_ticks").forGetter(CompanyConfig::loadedVillageScanIntervalTicks),
            Codec.INT.fieldOf("price_history_limit").forGetter(CompanyConfig::priceHistoryLimit),
            Codec.INT.fieldOf("financial_report_interval_ticks").forGetter(CompanyConfig::financialReportIntervalTicks),
            Codec.INT.fieldOf("financial_report_history_limit").forGetter(CompanyConfig::financialReportHistoryLimit),
            Codec.INT.fieldOf("senior_villager_level").forGetter(CompanyConfig::seniorVillagerLevel),
            Codec.DOUBLE.fieldOf("senior_derivative_demand_multiplier").forGetter(CompanyConfig::seniorDerivativeDemandMultiplier),
            ReportRules.CODEC.fieldOf("report").forGetter(CompanyConfig::report)
    ).apply(instance, CompanyConfig::new));

    public static CompanyConfig defaults() {
        return new CompanyConfig(
                500L,
                0L,
                48,
                8,
                200,
                160,
                2400,
                32,
                4,
                2.0D,
                new ReportRules(0.08D, 1.0D, 1.0D, 1, 0.7D, 0.3D, 0.0D, 1.0D, -1.0D, 1.0D)
        );
    }

    public record ReportRules(
            double significantChangeThreshold,
            double attractionMultiplier,
            double attractionDemandFloor,
            int minimumAttractionVolume,
            double performanceNavWeight,
            double performanceIncomeWeight,
            double minReportChangeRatio,
            double maxReportChangeRatio,
            double minPerformanceChangeRatio,
            double maxPerformanceChangeRatio
    ) {
        public static final Codec<ReportRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("significant_change_threshold").forGetter(ReportRules::significantChangeThreshold),
                Codec.DOUBLE.fieldOf("attraction_multiplier").forGetter(ReportRules::attractionMultiplier),
                Codec.DOUBLE.fieldOf("attraction_demand_floor").forGetter(ReportRules::attractionDemandFloor),
                Codec.INT.fieldOf("minimum_attraction_volume").forGetter(ReportRules::minimumAttractionVolume),
                Codec.DOUBLE.fieldOf("performance_nav_weight").forGetter(ReportRules::performanceNavWeight),
                Codec.DOUBLE.fieldOf("performance_income_weight").forGetter(ReportRules::performanceIncomeWeight),
                Codec.DOUBLE.fieldOf("min_report_change_ratio").forGetter(ReportRules::minReportChangeRatio),
                Codec.DOUBLE.fieldOf("max_report_change_ratio").forGetter(ReportRules::maxReportChangeRatio),
                Codec.DOUBLE.fieldOf("min_performance_change_ratio").forGetter(ReportRules::minPerformanceChangeRatio),
                Codec.DOUBLE.fieldOf("max_performance_change_ratio").forGetter(ReportRules::maxPerformanceChangeRatio)
        ).apply(instance, ReportRules::new));
    }
}
